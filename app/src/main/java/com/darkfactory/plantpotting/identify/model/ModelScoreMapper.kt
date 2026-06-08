package com.darkfactory.plantpotting.identify.model

import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.identify.IdentificationResult
import com.darkfactory.plantpotting.identify.ModelLabelsList
import com.darkfactory.plantpotting.identify.PerSpeciesThresholds
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converts raw softmax scores into a [MappedScore]. Implements the PLANTPOTTING-0003 §4.3
 * confidence policy verbatim:
 *
 * - `bestProb ≥ thresholds.highConfidencePlain` AND mapping resolves   → high-confidence direct
 * - `bestProb ∈ [margin_min, plain)` AND `(best - second) ≥ delta` AND mapping resolves
 *                                                                       → high-confidence with margin
 * - Otherwise                                                            → low-confidence
 *                                                                         (candidates = up-to-K mapped top-K)
 *
 * Hard failures (malformed scores, unknown KB id) throw — they are *not* converted to
 * low-confidence verdicts. Use [com.darkfactory.plantpotting.identify.IdentificationFailureException]
 * for those.
 */
@Singleton
class ModelScoreMapper
    @Inject
    constructor(
        @ModelLabelsList private val labels: List<String>,
        private val mapping: ModelLabelMap,
        private val kb: KnowledgeBase,
        private val thresholds: ModelManifest.Thresholds,
        @PerSpeciesThresholds private val perSpeciesThresholds: Map<String, Float> = emptyMap(),
        // PLANTPOTTING-0012 Phase 4 — targeted pothos↔Pilea boundary gate (default empty = no-op).
        private val boundaryPairs: List<ModelManifest.BoundaryPair> = emptyList(),
    ) {
        fun map(scores: FloatArray): MappedScore {
            require(scores.size == labels.size) {
                "Scores length ${scores.size} does not match labels length ${labels.size}"
            }
            val ranked = scores.indices.sortedByDescending { scores[it] }
            if (ranked.isEmpty()) {
                return lowConfidence(emptyList())
            }
            val bestIdx = ranked[0]
            val bestProb = scores[bestIdx]
            val secondProb = if (ranked.size > 1) scores[ranked[1]] else 0f

            // PLANTPOTTING-0010 D3 — raw top-1 facts, captured BEFORE (and independent of) the
            // verdict logic below. A confident-but-unmapped top routes to "Add this plant".
            val topLabel = labels[bestIdx]
            val topIsMapped = mapping.lookup(topLabel) != null
            val topIsConfidentUnmapped = !topIsMapped && bestProb >= thresholds.highConfidencePlain

            val mappedCandidates =
                ranked
                    .asSequence()
                    .mapNotNull { idx -> candidateForIndex(idx, scores[idx]) }
                    .take(thresholds.topKCandidates)
                    .toList()

            val bestEntry = mapping.lookup(labels[bestIdx])
            // PLANTPOTTING-0005 §5.3 — per-species override consulted before the global
            // `highConfidencePlain`. Override only the `_plain` value; margin overrides are
            // deferred per §4.3 ("premature surface"). The map ships empty by default; a
            // species id is present only if §5.7's probe-evidence gate fired.
            val bestSpeciesId = bestEntry?.kbSpeciesId
            val plainThreshold =
                if (bestSpeciesId != null) {
                    perSpeciesThresholds[bestSpeciesId] ?: thresholds.highConfidencePlain
                } else {
                    thresholds.highConfidencePlain
                }
            val highConfDirect = bestProb >= plainThreshold && bestEntry != null
            val highConfMargin =
                bestProb >= thresholds.highConfidenceMarginMin &&
                    (bestProb - secondProb) >= thresholds.highConfidenceMarginDelta &&
                    bestEntry != null

            // PLANTPOTTING-0011 Phase 3 — abstention veto. A confident verdict whose top-1/top-2
            // margin is too narrow is downgraded to low-confidence (routed to the picker with the
            // same mapped candidates) rather than shown as a confident card. This is an *additional*
            // gate on top of the existing margin branch, not a replacement. At the default margin of
            // `0f` the condition `(bestProb - secondProb) < 0f` can never hold (scores are ranked
            // descending), so this is a no-op and the baseline stays byte-for-byte unchanged.
            val abstainOnNarrowMargin =
                (bestProb - secondProb) < thresholds.highConfidenceAbstainMargin

            // PLANTPOTTING-0012 Phase 4 — pothos↔Pilea boundary gate (Candidate B). If the raw top-1
            // resolves to a boundary trigger species (e.g. pilea-peperomioides), force the picker and
            // guarantee both pair members are visible — independent of confidence, because the target
            // error (pothos→Pilea @ 0.9661) has no second-place mass for the abstain margin to bite.
            // Scoped to top-1 = trigger, so a pothos-dominant (correct) result is never affected.
            //
            // PLANTPOTTING-0013 Phase A3 — conditional direct card. The boundary route is now refined:
            // it fires (→ picker) UNLESS the trigger species has an elevated per-species threshold
            // `T_pilea` that `bestProb` clears, in which case control FALLS THROUGH to the normal
            // high-confidence/abstain path below (which emits a direct card). The elevated bar composes
            // WITH the boundary rule — it never replaces it. The dormant-safe default: with no
            // per-species threshold for the trigger (the production state until A4 activates it), the
            // gate is byte-for-byte the 0012 strict-picker. `T_pilea` is CI-bound > 0.9661 (the
            // documented pothos→Pilea ceiling), so a pothos misread as Pilea @ 0.9661 can never clear it
            // and never reach a direct Pilea card. See docs/sprints/evidence/PLANTPOTTING-0013/.
            val boundaryPair = bestEntry?.let { be -> boundaryPairs.firstOrNull { it.top1KbSpeciesId == be.kbSpeciesId } }
            if (boundaryPair != null) {
                val directCardAllowed =
                    perSpeciesThresholds[boundaryPair.top1KbSpeciesId]?.let { bestProb >= it } == true
                if (!directCardAllowed) {
                    return lowConfidence(
                        candidates = boundaryCandidates(boundaryPair, mappedCandidates, ranked, scores),
                        topLabel = topLabel,
                        topProbability = bestProb,
                        topIsMapped = topIsMapped,
                        topIsConfidentUnmapped = topIsConfidentUnmapped,
                    )
                }
                // else: bestProb ≥ T_pilea — fall through to the high-conf/abstain path, which emits the
                // direct card via the per-species `plainThreshold` already computed above.
            }

            return if ((highConfDirect || highConfMargin) && !abstainOnNarrowMargin) {
                val species =
                    kb.findSpecies(bestEntry!!.kbSpeciesId)
                        ?: error("plant_class_map points at unknown KB species '${bestEntry.kbSpeciesId}'")
                MappedScore(
                    result =
                        IdentificationResult(
                            speciesId = bestEntry.kbSpeciesId,
                            displayName = species.commonNames.firstOrNull() ?: species.scientificName,
                            source = IdSource.ON_DEVICE_MODEL,
                            lowConfidence = false,
                        ),
                    candidates = mappedCandidates,
                    topLabel = topLabel,
                    topProbability = bestProb,
                    topIsMapped = topIsMapped,
                    topIsConfidentUnmapped = topIsConfidentUnmapped,
                )
            } else {
                lowConfidence(
                    candidates = mappedCandidates,
                    topLabel = topLabel,
                    topProbability = bestProb,
                    topIsMapped = topIsMapped,
                    topIsConfidentUnmapped = topIsConfidentUnmapped,
                )
            }
        }

        /**
         * Candidate bundle for a fired boundary gate: the existing top-K mapped candidates (so the
         * true raw-top — e.g. Pilea — stays first and is not buried), plus any [BoundaryPair]
         * `surfaceKbSpeciesIds` member that fell outside top-K (e.g. pothos at a low rank), appended
         * at its own best score. De-duplicated by speciesId so a pair member already present in top-K
         * is never duplicated.
         */
        private fun boundaryCandidates(
            pair: ModelManifest.BoundaryPair,
            topKCandidates: List<Candidate>,
            ranked: List<Int>,
            scores: FloatArray,
        ): List<Candidate> {
            val present = topKCandidates.mapTo(mutableSetOf()) { it.speciesId }
            val out = topKCandidates.toMutableList()
            for (kbId in pair.surfaceKbSpeciesIds) {
                if (kbId in present) continue
                val idx = ranked.firstOrNull { mapping.lookup(labels[it])?.kbSpeciesId == kbId } ?: continue
                candidateForIndex(idx, scores[idx])?.let {
                    out += it
                    present += kbId
                }
            }
            return out
        }

        private fun candidateForIndex(
            idx: Int,
            prob: Float,
        ): Candidate? {
            val entry = mapping.lookup(labels[idx]) ?: return null
            val species = kb.findSpecies(entry.kbSpeciesId) ?: return null
            return Candidate(
                speciesId = entry.kbSpeciesId,
                displayName = species.commonNames.firstOrNull() ?: species.scientificName,
                probability = prob,
            )
        }

        private fun lowConfidence(
            candidates: List<Candidate>,
            topLabel: String = "",
            topProbability: Float = 0f,
            topIsMapped: Boolean = false,
            topIsConfidentUnmapped: Boolean = false,
        ): MappedScore =
            MappedScore(
                result =
                    IdentificationResult(
                        speciesId = "",
                        displayName = "",
                        source = IdSource.ON_DEVICE_MODEL,
                        lowConfidence = true,
                    ),
                candidates = candidates,
                topLabel = topLabel,
                topProbability = topProbability,
                topIsMapped = topIsMapped,
                topIsConfidentUnmapped = topIsConfidentUnmapped,
            )
    }
