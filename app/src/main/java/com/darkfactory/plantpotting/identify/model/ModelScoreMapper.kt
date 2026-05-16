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

            return if (highConfDirect || highConfMargin) {
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
                )
            } else {
                lowConfidence(mappedCandidates)
            }
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

        private fun lowConfidence(candidates: List<Candidate>): MappedScore =
            MappedScore(
                result =
                    IdentificationResult(
                        speciesId = "",
                        displayName = "",
                        source = IdSource.ON_DEVICE_MODEL,
                        lowConfidence = true,
                    ),
                candidates = candidates,
            )
    }
