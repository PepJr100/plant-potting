package com.darkfactory.plantpotting.identify.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0005 §0.6 — contract-lock test. RED today; flips GREEN after §5.2
 * lands `ModelManifest.perSpeciesThresholds: Map<String, Float>`.
 *
 * Reflection-based so the contract test does not depend on the field having a
 * particular runtime value — only that the shape is on the class.
 */
class PerSpeciesThresholdsContractTest {
    @Test
    fun modelManifestExposesPerSpeciesThresholdsAsMap() {
        val field = ModelManifest::class.java.declaredFields.firstOrNull { it.name == "perSpeciesThresholds" }
        assertThat(field).isNotNull()
        assertThat(Map::class.java.isAssignableFrom(field!!.type)).isTrue()
    }
}
