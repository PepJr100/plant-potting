package com.darkfactory.plantpotting.identify.model

import com.darkfactory.plantpotting.identify.IdentificationResult

/**
 * Result of [ModelScoreMapper.map]: the identification verdict plus the (possibly empty)
 * list of mapped top-K candidates. Per PLANTPOTTING-0003 §4.3:
 *
 * - High-confidence paths return a non-empty [result] with `lowConfidence = false` and
 *   `speciesId` resolved; [candidates] is supplemental display data for any UI that
 *   wants to show alternatives.
 * - Low-confidence paths return [result] with `lowConfidence = true` and empty
 *   `speciesId`/`displayName`; the caller routes to the picker using [candidates].
 */
data class MappedScore(
    val result: IdentificationResult,
    val candidates: List<Candidate>,
    // PLANTPOTTING-0010 D3 — raw top-1 (`ranked[0]`) info, independent of the high/low verdict, so a
    // *confident-but-unmapped* class (a strong prediction with no KB entry) can be routed to the
    // "Add this plant" wireframe instead of collapsing silently into the low-confidence verdict.
    val topLabel: String = "",
    val topProbability: Float = 0f,
    val topIsMapped: Boolean = false,
    /** True iff the raw top-1 is unmapped AND clears the global high-confidence-plain threshold. */
    val topIsConfidentUnmapped: Boolean = false,
)
