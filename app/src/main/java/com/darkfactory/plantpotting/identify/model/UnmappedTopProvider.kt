package com.darkfactory.plantpotting.identify.model

/**
 * PLANTPOTTING-0010 D3 — optional side-channel exposing the raw top-1 prediction of the most recent
 * `identify(jpeg)` call, so a *confident-but-unmapped* class (a strong prediction with no KB entry)
 * can be routed to the "Add this plant" wireframe instead of collapsing into the low-confidence
 * verdict. Kept separate from [CandidateProvider] (which surfaces mapped KB candidates) per the
 * §single-property marker convention.
 *
 * Production's `OnDevicePlantIdentifier` implements this; test fakes are free not to.
 */
interface UnmappedTopProvider {
    /** Raw top-1 facts for the most recent identify, or null if none has run. */
    val mostRecentTop: TopPrediction?
}

/**
 * @param modelClassLabel the verbatim model label of the top-1 class.
 * @param probabilityPct integer confidence (0..100) of the top-1.
 * @param isConfidentUnmapped true iff the top-1 is unmapped AND clears the high-confidence-plain
 *   threshold — the predicate that carves the "Add this plant" slice out of the low-confidence path.
 */
data class TopPrediction(
    val modelClassLabel: String,
    val probabilityPct: Int,
    val isConfidentUnmapped: Boolean,
)
