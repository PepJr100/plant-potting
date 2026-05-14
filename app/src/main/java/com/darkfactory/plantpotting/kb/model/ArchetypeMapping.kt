package com.darkfactory.plantpotting.kb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ArchetypeMapping {
    @Serializable
    @SerialName("single")
    data class Single(
        val archetypeId: String,
    ) : ArchetypeMapping()

    @Serializable
    @SerialName("blend")
    data class Blend(
        val primaryArchetypeId: String,
        val secondaryArchetypeId: String,
        val primaryPct: Int,
    ) : ArchetypeMapping()
}
