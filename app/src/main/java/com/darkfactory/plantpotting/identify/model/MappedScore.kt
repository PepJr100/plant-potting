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
)
