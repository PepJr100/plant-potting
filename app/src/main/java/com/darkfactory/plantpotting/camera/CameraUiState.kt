package com.darkfactory.plantpotting.camera

sealed interface CameraUiState {
    data object Idle : CameraUiState

    data object Capturing : CameraUiState

    data object Identifying : CameraUiState

    data class Success(
        val speciesId: String,
    ) : CameraUiState

    data class Failure(
        val reason: String,
    ) : CameraUiState
}
