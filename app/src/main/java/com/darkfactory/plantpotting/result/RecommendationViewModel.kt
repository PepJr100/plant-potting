package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.darkfactory.plantpotting.recommend.RecommendationEngine
import com.darkfactory.plantpotting.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        engine: RecommendationEngine,
    ) : ViewModel() {
        private val rawId: String = savedStateHandle.get<String>(Routes.ARG_SPECIES_ID).orEmpty()
        private val speciesId: String =
            runCatching { URLDecoder.decode(rawId, "UTF-8") }
                .getOrDefault(rawId)

        private val _state = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
        val state: StateFlow<RecommendationUiState> = _state.asStateFlow()

        init {
            _state.value =
                runCatching {
                    val rec = engine.recommend(speciesId)
                    RecommendationUiState.Ready(
                        archetypeName = rec.archetypeName,
                        rationale = rec.rationale,
                        recipe = rec.recipe,
                        isBlend = rec.isBlend,
                    )
                }.getOrElse { RecommendationUiState.NotFound }
        }
    }
