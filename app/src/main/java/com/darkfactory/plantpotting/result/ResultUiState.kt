package com.darkfactory.plantpotting.result

data class ResultUiState(
    val scientificName: String = "",
    val commonName: String = "",
    val speciesId: String = "",
    val notFound: Boolean = false,
)
