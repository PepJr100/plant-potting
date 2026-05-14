package com.darkfactory.plantpotting.kb.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val ingredient: String,
    val proportionPct: Int,
    val notes: String? = null,
)
