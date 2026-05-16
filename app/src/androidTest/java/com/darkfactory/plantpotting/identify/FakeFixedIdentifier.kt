package com.darkfactory.plantpotting.identify

import com.darkfactory.plantpotting.identify.model.Candidate
import com.darkfactory.plantpotting.identify.model.CandidateProvider

/**
 * Deterministic identifier used by instrumentation tests. Returns the same
 * `speciesId` for every call, irrespective of the bytes passed in.
 *
 * PLANTPOTTING-0003 §5.9: production wiring is the on-device model, so the
 * fake reports `source = IdSource.ON_DEVICE_MODEL` to keep test assertions
 * aligned with what a real device-aware run would surface (the
 * "On-device match" badge, not the legacy stub copy).
 *
 * PLANTPOTTING-0005 §4.1 — implements [CandidateProvider] so low-confidence
 * tests (e.g. `LowConfidenceFlowTest`) can seed the chip row without having
 * to subclass the fake. `seedCandidates` defaults to empty, which preserves
 * the existing zero-candidate behaviour for callers that don't need it.
 */
class FakeFixedIdentifier(
    private val speciesId: String = "monstera-deliciosa",
    private val displayName: String = "Swiss cheese plant",
    private val source: IdSource = IdSource.ON_DEVICE_MODEL,
    private val lowConfidence: Boolean = false,
    seedCandidates: List<Candidate> = emptyList(),
) : PlantIdentifier,
    CandidateProvider {
    override val mostRecentCandidates: List<Candidate> = seedCandidates

    override suspend fun identify(jpeg: ByteArray): IdentificationResult =
        IdentificationResult(
            speciesId = speciesId,
            displayName = displayName,
            source = source,
            lowConfidence = lowConfidence,
        )
}
