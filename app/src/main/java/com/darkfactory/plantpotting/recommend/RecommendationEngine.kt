package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.model.RecipeIngredient

interface RecommendationEngine {
    fun recommend(speciesId: String): Recommendation

    /**
     * Recommend a recipe by archetype id (PLANTPOTTING-0003 §6.8). Used by the
     * "I don't know — pick by archetype" path so the user can still get a substrate
     * recipe when their plant doesn't match any KB species.
     *
     * Returns a [Recommendation] whose `archetypeName` is the archetype's display
     * name, recipe is the archetype's stock recipe (sums to 100), `rationale` does
     * not name a species, and `isBlend = false`.
     */
    fun recommendByArchetype(archetypeId: String): Recommendation
}

data class Recommendation(
    val archetypeName: String,
    val recipe: List<RecipeIngredient>,
    val rationale: String,
    val isBlend: Boolean,
)
