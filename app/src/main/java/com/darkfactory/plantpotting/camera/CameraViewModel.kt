package com.darkfactory.plantpotting.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkfactory.plantpotting.identify.PlantIdentifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val identifier: PlantIdentifier,
) : ViewModel() {

    private val _state = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val state: StateFlow<CameraUiState> = _state.asStateFlow()

    private val _navigate = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigate: SharedFlow<String> = _navigate.asSharedFlow()

    /** Invoked by the host once a JPEG has been captured. */
    fun onCaptureReady(jpeg: ByteArray) {
        if (_state.value is CameraUiState.Capturing || _state.value is CameraUiState.Identifying) {
            return
        }
        viewModelScope.launch {
            _state.value = CameraUiState.Capturing
            _state.value = CameraUiState.Identifying
            runCatching { identifier.identify(jpeg) }
                .onSuccess { result ->
                    _state.value = CameraUiState.Success(result.speciesId)
                    _navigate.tryEmit(result.speciesId)
                }
                .onFailure { e ->
                    _state.value = CameraUiState.Failure(e.message ?: "identification failed")
                }
        }
    }

    fun reset() {
        _state.value = CameraUiState.Idle
    }

    fun onCaptureFailed(reason: String) {
        _state.value = CameraUiState.Failure(reason)
    }
}
