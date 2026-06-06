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
        // PLANTPOTTING-0010 (review feedback) — the identified plant, so the potting-mix page leads
        // with the plant name + picture. Empty when reached via "Browse mixes" (archetype-only path).
        val speciesId: String = "",
        val plantName: String = "",
    ) : RecommendationUiState
}
