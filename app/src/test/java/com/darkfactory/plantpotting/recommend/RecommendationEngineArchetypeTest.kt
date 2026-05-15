package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0003 §6.7 — `recommendByArchetype` returns the archetype's stock recipe
 * (summing to 100) with a rationale that does **not** name a species, and `isBlend = false`.
 *
 * RED before §6.8 lands `recommendByArchetype` on `KbRecommendationEngine`.
 */
class RecommendationEngineArchetypeTest {
    private val aroidChunky =
        Archetype(
            id = "aroid-chunky",
            displayName = "Aroid Chunky",
            shortDescription = "Bark-dominant, fast-reaerating mix.",
            recipe =
                listOf(
                    RecipeIngredient("Pine or orchid bark", 40),
                    RecipeIngredient("Coco coir", 25),
                    RecipeIngredient("Perlite or pumice", 20),
                    RecipeIngredient("Long-fibre sphagnum", 10),
                    RecipeIngredient("Horticultural charcoal", 5),
                ),
            rationaleTemplate = "A chunky aroid mix suits {species}: hemi-epiphytic roots want air.",
            citations = listOf("Brief"),
        )

    private val standardHouseplant =
        Archetype(
            id = "standard-houseplant",
            displayName = "Standard Houseplant",
            shortDescription = "Balanced coir mix.",
            recipe =
                listOf(
                    RecipeIngredient("Coco coir", 60),
                    RecipeIngredient("Perlite", 30),
                    RecipeIngredient("Bark fines", 10),
                ),
            rationaleTemplate = "A standard coir mix suits {species}: typical fibrous-rooted plants.",
            citations = listOf("Brief"),
        )

    private val species =
        Species(
            id = "monstera-deliciosa",
            scientificName = "Monstera deliciosa",
            commonNames = listOf("Swiss cheese plant"),
            aliases = emptyList(),
            mapping = ArchetypeMapping.Single("aroid-chunky"),
            speciesRationale = "Hemi-epiphytic aroid.",
            citations = listOf("Brief"),
        )

    private val kb =
        KnowledgeBase(
            archetypes = mapOf("aroid-chunky" to aroidChunky, "standard-houseplant" to standardHouseplant),
            species = listOf(species),
            speciesIndex = mapOf("monstera-deliciosa" to species),
        )

    @Test
    fun aroidChunkyReturnsArchetypeNameAndRecipe() {
        val rec = KbRecommendationEngine(kb).recommendByArchetype("aroid-chunky")
        assertThat(rec.archetypeName).isEqualTo("Aroid Chunky")
        assertThat(rec.recipe).hasSize(5)
        assertThat(rec.recipe.sumOf { it.proportionPct }).isEqualTo(100)
        assertThat(rec.isBlend).isFalse()
    }

    @Test
    fun rationaleDoesNotNameAnySpecies() {
        val rec = KbRecommendationEngine(kb).recommendByArchetype("aroid-chunky")
        assertThat(rec.rationale).doesNotContain("Monstera")
        assertThat(rec.rationale).doesNotContain("{species}")
    }

    @Test
    fun standardHouseplantPath() {
        val rec = KbRecommendationEngine(kb).recommendByArchetype("standard-houseplant")
        assertThat(rec.archetypeName).isEqualTo("Standard Houseplant")
        assertThat(rec.recipe.sumOf { it.proportionPct }).isEqualTo(100)
    }

    @Test
    fun unknownArchetypeThrows() {
        try {
            KbRecommendationEngine(kb).recommendByArchetype("no-such-archetype")
            assertThat("did not throw").isEqualTo("threw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("no-such-archetype")
        }
    }
}
