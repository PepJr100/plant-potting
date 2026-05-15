package com.darkfactory.plantpotting.identify

/**
 * Thrown by `OnDevicePlantIdentifier` (and its collaborators) when identification cannot
 * complete because of an *infrastructure* problem: model load failure, malformed JPEG,
 * native interpreter exception, missing asset. These are **not** low-confidence results
 * — per PLANTPOTTING-0003 §4.3 the low-confidence path is reserved for *uncertainty*,
 * and surfacing model breakage as user uncertainty would be wrong.
 *
 * The `CameraViewModel` catches this exception and transitions to `CameraUiState.Failure`
 * so the user is asked to retake the photo.
 */
class IdentificationFailureException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
