package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class ResultViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        kb: KnowledgeBase,
    ) : ViewModel() {
        private val rawId: String =
            savedStateHandle.get<String>(Routes.ARG_SPECIES_ID).orEmpty()

        private val speciesId: String =
            runCatching { URLDecoder.decode(rawId, "UTF-8") }
                .getOrDefault(rawId)

        private val source: IdSource =
            savedStateHandle
                .get<String>(Routes.ARG_SOURCE)
                ?.let { runCatching { IdSource.valueOf(it) }.getOrNull() }
                ?: IdSource.STUB_DETERMINISTIC

        private val lowConfidence: Boolean =
            savedStateHandle.get<Boolean>(Routes.ARG_LOW_CONFIDENCE) ?: false

        private val _state = MutableStateFlow(initialState(kb))
        val state: StateFlow<ResultUiState> = _state.asStateFlow()

        private fun initialState(kb: KnowledgeBase): ResultUiState {
            val species =
                kb.findSpecies(speciesId) ?: return ResultUiState(
                    speciesId = speciesId,
                    notFound = true,
                    source = source,
                    lowConfidence = lowConfidence,
                )
            return ResultUiState(
                scientificName = species.scientificName,
                commonName = species.commonNames.firstOrNull().orEmpty(),
                speciesId = species.id,
                notFound = false,
                source = source,
                lowConfidence = lowConfidence,
            )
        }
    }
