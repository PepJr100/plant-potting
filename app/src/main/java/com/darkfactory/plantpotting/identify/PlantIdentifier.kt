package com.darkfactory.plantpotting.identify

interface PlantIdentifier {
    suspend fun identify(jpeg: ByteArray): IdentificationResult
}

data class IdentificationResult(
    val speciesId: String,
    val displayName: String,
    val source: IdSource,
    val lowConfidence: Boolean = false,
)

enum class IdSource {
    STUB_DETERMINISTIC,
    STUB_RANDOM,
    ON_DEVICE_MODEL,
    CLOUD,
}
