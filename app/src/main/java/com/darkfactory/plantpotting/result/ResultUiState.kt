package com.darkfactory.plantpotting.result

import com.darkfactory.plantpotting.identify.IdSource

data class ResultUiState(
    val scientificName: String = "",
    val commonName: String = "",
    val speciesId: String = "",
    val notFound: Boolean = false,
    val source: IdSource = IdSource.STUB_DETERMINISTIC,
    val lowConfidence: Boolean = false,
)
