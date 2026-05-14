package com.darkfactory.plantpotting.identify

/**
 * Deterministic identifier used by instrumentation tests. Returns the same
 * `speciesId` for every call, irrespective of the bytes passed in.
 */
class FakeFixedIdentifier(
    private val speciesId: String = "monstera-deliciosa",
    private val displayName: String = "Swiss cheese plant",
) : PlantIdentifier {
    override suspend fun identify(jpeg: ByteArray): IdentificationResult =
        IdentificationResult(
            speciesId = speciesId,
            displayName = displayName,
            source = IdSource.STUB_DETERMINISTIC,
        )
}
