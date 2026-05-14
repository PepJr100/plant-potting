package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KbRecommendationEngineTest {

    private val standard = Archetype(
        id = "standard",
        displayName = "Standard",
        shortDescription = "standard mix",
        recipe = listOf(
            RecipeIngredient("coir", 60),
            RecipeIngredient("perlite", 30),
            RecipeIngredient("bark", 10),
        ),
        rationaleTemplate = "Standard mix suits {species}.",
        citations = listOf("Brief §4.4"),
    )

    private val gritty = Archetype(
        id = "gritty",
        displayName = "Gritty",
        shortDescription = "gritty mineral mix",
        recipe = listOf(
            RecipeIngredient("pumice", 50),
            RecipeIngredient("sand", 30),
            RecipeIngredient("bark", 20),
        ),
        rationaleTemplate = "Gritty mix suits {species}.",
        citations = listOf("Brief §4.4"),
    )

    private fun species(
        id: String,
        scientificName: String,
        mapping: ArchetypeMapping,
        rationale: String = "$scientificName is a test fixture.",
    ): Species = Species(
        id = id,
        scientificName = scientificName,
        commonNames = listOf("common"),
        aliases = emptyList(),
        mapping = mapping,
        speciesRationale = rationale,
        citations = listOf("Brief §3"),
    )

    private fun kb(species: List<Species>): KnowledgeBase {
        val archetypes = mapOf("standard" to standard, "gritty" to gritty)
        return KnowledgeBase(
            archetypes = archetypes,
            species = species,
            speciesIndex = species.associateBy { it.id },
        )
    }

    @Test
    fun singleMappingReturnsExactArchetypeRecipe() {
        val s = species("test-1", "Testus fixtura", ArchetypeMapping.Single("standard"))
        val engine = KbRecommendationEngine(kb(listOf(s)))
        val rec = engine.recommend("test-1")
        assertThat(rec.isBlend).isFalse()
        assertThat(rec.archetypeName).isEqualTo("Standard")
        assertThat(rec.recipe).isEqualTo(standard.recipe)
        assertThat(rec.rationale).contains("Testus fixtura")
    }

    @Test
    fun blendMappingReturnsRecipeSummingToOneHundred() {
        val s = species(
            id = "test-blend",
            scientificName = "Testus blendix",
            mapping = ArchetypeMapping.Blend(
                primaryArchetypeId = "standard",
                secondaryArchetypeId = "gritty",
                primaryPct = 60,
            ),
        )
        val engine = KbRecommendationEngine(kb(listOf(s)))
        val rec = engine.recommend("test-blend")
        assertThat(rec.isBlend).isTrue()
        assertThat(rec.archetypeName).contains("Standard")
        assertThat(rec.archetypeName).contains("Gritty")
        assertThat(rec.recipe.sumOf { it.proportionPct }).isEqualTo(100)
        assertThat(rec.rationale).contains("Testus blendix")
    }

    @Test
    fun blendMergesMatchingIngredientNames() {
        val s = species(
            id = "test-merge",
            scientificName = "Testus mergeus",
            mapping = ArchetypeMapping.Blend(
                primaryArchetypeId = "standard",
                secondaryArchetypeId = "gritty",
                primaryPct = 50,
            ),
        )
        val engine = KbRecommendationEngine(kb(listOf(s)))
        val rec = engine.recommend("test-merge")
        // Both archetypes contain "bark"; expect a single merged row.
        val barkRows = rec.recipe.filter { it.ingredient == "bark" }
        assertThat(barkRows).hasSize(1)
        // standard.bark=10 × 0.5 + gritty.bark=20 × 0.5 = 5 + 10 = 15
        assertThat(barkRows.single().proportionPct).isEqualTo(15)
    }

    @Test
    fun unknownSpeciesIdRaisesIllegalArgument() {
        val engine = KbRecommendationEngine(kb(emptyList()))
        try {
            engine.recommend("ghost-species")
            throw AssertionError("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("ghost-species")
        }
    }

    @Test
    fun blendWithFractionalSplitStillSumsToOneHundred() {
        // 33/67 split produces fractional intermediate values; verify rounding lands at 100.
        val s = species(
            id = "test-33",
            scientificName = "Testus fractus",
            mapping = ArchetypeMapping.Blend(
                primaryArchetypeId = "standard",
                secondaryArchetypeId = "gritty",
                primaryPct = 33,
            ),
        )
        val engine = KbRecommendationEngine(kb(listOf(s)))
        val rec = engine.recommend("test-33")
        assertThat(rec.recipe.sumOf { it.proportionPct }).isEqualTo(100)
    }
}
