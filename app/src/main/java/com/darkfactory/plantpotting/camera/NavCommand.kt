package com.darkfactory.plantpotting.camera

import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.identify.model.Candidate

/**
 * Outcome of an `identify(jpeg)` call, mapped to a navigation intent.
 *
 * Per PLANTPOTTING-0003 §4.3 / §5.7:
 * - [Success]: high-confidence hit; the camera flow navigates to `result/...`.
 * - [LowConfidence]: identifier returned `lowConfidence = true`; navigate to the picker
 *   with up-to-K mapped candidates so the user can choose.
 * - [Failure]: hard error (e.g., `IdentificationFailureException`). The camera UI shows
 *   the existing `CameraUiState.Failure` state; the nav host ignores this variant.
 */
sealed class NavCommand {
    data class Success(
        val speciesId: String,
        val source: IdSource,
        val lowConfidence: Boolean,
        // PLANTPOTTING-0010 D2 — integer confidence (0..100) from the on-device top candidate's
        // softmax, surfaced via the CandidateProvider side-channel; null for stub flows.
        val confidencePct: Int? = null,
    ) : NavCommand()

    data class LowConfidence(
        val candidates: List<Candidate>,
    ) : NavCommand()

    /**
     * PLANTPOTTING-0010 Pillar B — a confident-but-unmapped model class (strong prediction, no KB
     * entry). Carved out of the low-confidence path; routes to the "Add this plant" wireframe.
     */
    data class AddPlant(
        val modelClassLabel: String,
        val confidencePct: Int,
    ) : NavCommand()

    data class Failure(
        val message: String,
    ) : NavCommand()
}
