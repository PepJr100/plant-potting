package com.darkfactory.plantpotting.result

import com.darkfactory.plantpotting.identify.IdSource

data class ResultUiState(
    val scientificName: String = "",
    val commonName: String = "",
    val speciesId: String = "",
    val notFound: Boolean = false,
    val source: IdSource = IdSource.STUB_DETERMINISTIC,
    val lowConfidence: Boolean = false,
    // PLANTPOTTING-0010 D2 — integer confidence (0..100) from the on-device path; null ⇒ render
    // no %/bar (stub & picker flows carry no probability).
    val confidencePct: Int? = null,
    // PLANTPOTTING-0010 Phase 4 — flips true after the user taps "Save to My Plants".
    val saved: Boolean = false,
)
