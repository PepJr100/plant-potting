package com.darkfactory.plantpotting.identify.model

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0005 §5.1 — covers both branches of the new per-species threshold
 * override:
 *  - `perSpeciesOverrideUsedWhenPresent` — manifest seeded with
 *    `perSpeciesThresholds = mapOf("monstera-deliciosa" to 0.35f)`, top-1 = 0.40f →
 *    high-confidence (under the override, even though 0.40 < global 0.55).
 *  - `globalThresholdUsedWhenNoOverride` — empty override map, top-1 = 0.40f →
 *    low-confidence (rejected by the global 0.55 default).
 *
 * The override only affects `highConfidencePlain`; margin path is unchanged. Setting
 * margin_min above the test scores ensures the margin path doesn't accidentally rescue
 * the global case (which would mask the override's effect).
 */
class ModelScoreMapperPerSpeciesThresholdTest {
    private val thresholds =
        ModelManifest.Thresholds(
            highConfidencePlain = 0.55f,
            highConfidenceMarginMin = 0.50f, // above 0.40 so the margin path can't rescue
            highConfidenceMarginDelta = 0.18f,
            topKCandidates = 3,
        )

    private val labels = listOf("Monstera deliciosa")
    private val mapping = modelLabelMap("Monstera deliciosa" to "monstera-deliciosa")
    private val kb = fixtureKb()

    @Test
    fun perSpeciesOverrideUsedWhenPresent() {
        val mapper =
            ModelScoreMapper(
                labels = labels,
                mapping = mapping,
                kb = kb,
                thresholds = thresholds,
                perSpeciesThresholds = mapOf("monstera-deliciosa" to 0.35f),
            )
        val out = mapper.map(floatArrayOf(0.40f))
        assertThat(out.result.speciesId).isEqualTo("monstera-deliciosa")
        assertThat(out.result.lowConfidence).isFalse()
    }

    @Test
    fun globalThresholdUsedWhenNoOverride() {
        val mapper =
            ModelScoreMapper(
                labels = labels,
                mapping = mapping,
                kb = kb,
                thresholds = thresholds,
                perSpeciesThresholds = emptyMap(),
            )
        val out = mapper.map(floatArrayOf(0.40f))
        assertThat(out.result.lowConfidence).isTrue()
    }

    private fun modelLabelMap(vararg pairs: Pair<String, String>): ModelLabelMap {
        val entries =
            pairs.associate { (label, kbId) ->
                ModelLabelMap.normalise(label) to ModelLabelMap.Entry(kbSpeciesId = kbId, alias = false)
            }
        val klass = ModelLabelMap::class.java
        val ctor = klass.declaredConstructors.single()
        ctor.isAccessible = true
        return ctor.newInstance(1, "ml/aiy_plants_v1/labels.csv", entries) as ModelLabelMap
    }

    private fun fixtureKb(): KnowledgeBase {
        val archetype =
            Archetype(
                id = "standard",
                displayName = "Standard",
                shortDescription = "",
                recipe = listOf(RecipeIngredient("coir", 100)),
                rationaleTemplate = "Suits {species}.",
                citations = listOf("Brief"),
            )
        val list =
            listOf(
                Species(
                    id = "monstera-deliciosa",
                    scientificName = "Monstera deliciosa",
                    commonNames = listOf("Swiss cheese plant"),
                    aliases = emptyList(),
                    mapping = ArchetypeMapping.Single("standard"),
                    speciesRationale = "Monstera deliciosa.",
                    citations = listOf("Brief"),
                ),
            )
        return KnowledgeBase(
            archetypes = mapOf("standard" to archetype),
            species = list,
            speciesIndex = list.associateBy { it.id },
        )
    }
}
