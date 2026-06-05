package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkfactory.plantpotting.persistence.PlantLogStore
import com.darkfactory.plantpotting.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

/**
 * PLANTPOTTING-0010 Pillar B — drives the "Add this plant" wireframe. Reads the confident-but-unmapped
 * model class label + confidence from nav args; on "Add this plant" it increments a local-only request
 * counter in the shared [PlantLogStore] (wireframe: logs only, never writes a KB species row).
 */
@HiltViewModel
class AddThisPlantViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val plantLogStore: PlantLogStore,
    ) : ViewModel() {
        private val modelClassLabel: String =
            savedStateHandle.get<String>(Routes.ARG_MODEL_CLASS_LABEL).orEmpty().let { raw ->
                runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
            }

        private val confidencePct: Int =
            savedStateHandle.get<Int>(Routes.ARG_CONFIDENCE_PCT)?.takeIf { it >= 0 } ?: 0

        private val _state =
            MutableStateFlow(
                AddThisPlantUiState(
                    modelClassLabel = modelClassLabel,
                    confidencePct = confidencePct,
                    requested = false,
                ),
            )
        val state: StateFlow<AddThisPlantUiState> = _state.asStateFlow()

        /** Each tap = one logged request (idempotent at the UI level via the `requested` flag). */
        fun onAddThisPlant() {
            if (_state.value.requested) return
            viewModelScope.launch {
                plantLogStore.recordAddPlantRequest(modelClassLabel)
                _state.update { it.copy(requested = true) }
            }
        }
    }

data class AddThisPlantUiState(
    val modelClassLabel: String,
    val confidencePct: Int,
    val requested: Boolean,
)
