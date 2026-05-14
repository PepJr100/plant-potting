package com.darkfactory.plantpotting.kb.model

import kotlinx.serialization.Serializable

@Serializable
data class Species(
    val id: String,
    val scientificName: String,
    val commonNames: List<String>,
    val aliases: List<String> = emptyList(),
    val mapping: ArchetypeMapping,
    val speciesRationale: String,
    val citations: List<String>,
)
