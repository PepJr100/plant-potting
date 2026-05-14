package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.model.RecipeIngredient

interface RecommendationEngine {
    fun recommend(speciesId: String): Recommendation
}

data class Recommendation(
    val archetypeName: String,
    val recipe: List<RecipeIngredient>,
    val rationale: String,
    val isBlend: Boolean,
)
