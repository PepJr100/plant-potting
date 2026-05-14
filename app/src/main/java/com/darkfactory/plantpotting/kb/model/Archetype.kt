package com.darkfactory.plantpotting.kb.model

import kotlinx.serialization.Serializable

@Serializable
data class Archetype(
    val id: String,
    val displayName: String,
    val shortDescription: String,
    val recipe: List<RecipeIngredient>,
    val rationaleTemplate: String,
    val citations: List<String>,
)
