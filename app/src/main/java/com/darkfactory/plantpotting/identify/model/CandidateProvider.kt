package com.darkfactory.plantpotting.identify.model

/**
 * Optional side-channel for an identifier to surface the top-K candidates of its most
 * recent `identify(jpeg)` call without growing the `PlantIdentifier` seam.
 *
 * Production's `OnDevicePlantIdentifier` implements this interface; the test fakes
 * (`StubPlantIdentifier`, `FakeFixedIdentifier`) do not. `CameraViewModel` performs an
 * `is CandidateProvider` check and falls back to an empty list otherwise.
 *
 * The single-property contract is intentionally minimal — a future variant that wanted
 * to expose latency telemetry or model-load metrics should layer that on a different
 * marker, not extend this one.
 */
interface CandidateProvider {
    /** Candidates corresponding to the most recent `identify(jpeg)` call. */
    val mostRecentCandidates: List<Candidate>
}
