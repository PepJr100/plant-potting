package com.darkfactory.plantpotting.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.identify.model.CandidateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel
    @Inject
    constructor(
        private val identifier: PlantIdentifier,
    ) : ViewModel() {
        private val _state = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
        val state: StateFlow<CameraUiState> = _state.asStateFlow()

        private val _navigate = MutableSharedFlow<NavCommand>(extraBufferCapacity = 1)
        val navigate: SharedFlow<NavCommand> = _navigate.asSharedFlow()

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
                        val command =
                            if (result.lowConfidence) {
                                val candidates =
                                    (identifier as? CandidateProvider)?.mostRecentCandidates ?: emptyList()
                                NavCommand.LowConfidence(candidates)
                            } else {
                                // D2 — the high-confidence winner is the first mapped candidate
                                // (ranked[0]); read its softmax via the side-channel and pass an
                                // integer percentage. Null when the identifier isn't a
                                // CandidateProvider (stub flows) — the result screen degrades to
                                // no %/bar.
                                val confidencePct =
                                    (identifier as? CandidateProvider)
                                        ?.mostRecentCandidates
                                        ?.firstOrNull()
                                        ?.probability
                                        ?.let { (it * 100).toInt().coerceIn(0, 100) }
                                NavCommand.Success(
                                    speciesId = result.speciesId,
                                    source = result.source,
                                    lowConfidence = false,
                                    confidencePct = confidencePct,
                                )
                            }
                        _state.value = CameraUiState.Success(result.speciesId)
                        _navigate.tryEmit(command)
                    }.onFailure { e ->
                        val message = e.message ?: "identification failed"
                        _state.value = CameraUiState.Failure(message)
                        _navigate.tryEmit(NavCommand.Failure(message))
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
