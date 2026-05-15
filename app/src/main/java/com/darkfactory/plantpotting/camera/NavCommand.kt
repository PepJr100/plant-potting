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
    ) : NavCommand()

    data class LowConfidence(
        val candidates: List<Candidate>,
    ) : NavCommand()

    data class Failure(
        val message: String,
    ) : NavCommand()
}
