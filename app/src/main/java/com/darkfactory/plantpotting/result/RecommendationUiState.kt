package com.darkfactory.plantpotting.result

import com.darkfactory.plantpotting.kb.model.RecipeIngredient

sealed interface RecommendationUiState {
    data object Loading : RecommendationUiState

    data object NotFound : RecommendationUiState

    data class Ready(
        val archetypeName: String,
        val rationale: String,
        val recipe: List<RecipeIngredient>,
        val isBlend: Boolean,
    ) : RecommendationUiState
}
