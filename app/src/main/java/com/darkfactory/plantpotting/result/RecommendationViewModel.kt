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
        private val rawSpeciesId: String =
            savedStateHandle.get<String>(Routes.ARG_SPECIES_ID).orEmpty()

        private val rawArchetypeId: String =
            savedStateHandle.get<String>(Routes.ARG_ARCHETYPE_ID).orEmpty()

        private val _state = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
        val state: StateFlow<RecommendationUiState> = _state.asStateFlow()

        init {
            _state.value = computeInitialState(engine)
        }

        private fun computeInitialState(engine: RecommendationEngine): RecommendationUiState =
            if (rawArchetypeId.isNotEmpty()) {
                val archetypeId =
                    runCatching { URLDecoder.decode(rawArchetypeId, "UTF-8") }
                        .getOrDefault(rawArchetypeId)
                runCatching {
                    val rec = engine.recommendByArchetype(archetypeId)
                    RecommendationUiState.Ready(
                        archetypeName = rec.archetypeName,
                        rationale = rec.rationale,
                        recipe = rec.recipe,
                        isBlend = rec.isBlend,
                    )
                }.getOrElse { RecommendationUiState.NotFound }
            } else {
                val speciesId =
                    runCatching { URLDecoder.decode(rawSpeciesId, "UTF-8") }
                        .getOrDefault(rawSpeciesId)
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
