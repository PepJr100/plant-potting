package com.darkfactory.plantpotting.kb

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KbValidationTest {
    private fun goodArchetype(id: String = "standard"): Archetype =
        Archetype(
            id = id,
            displayName = "Standard",
            shortDescription = "standard mix",
            recipe =
                listOf(
                    RecipeIngredient("coir", 60),
                    RecipeIngredient("perlite", 30),
                    RecipeIngredient("bark fines", 10),
                ),
            rationaleTemplate = "Standard mix suits {species} because of fibrous roots.",
            citations = listOf("Brief §4.4"),
        )

    private fun goodSpecies(
        id: String = "ficus-lyrata",
        archetypeId: String = "standard",
        aliases: List<String> = emptyList(),
        scientificName: String = "Ficus lyrata",
    ): Species =
        Species(
            id = id,
            scientificName = scientificName,
            commonNames = listOf("Fiddle-leaf fig"),
            aliases = aliases,
            mapping = ArchetypeMapping.Single(archetypeId),
            speciesRationale = "Ficus lyrata is a terrestrial fig.",
            citations = listOf("Brief §3.1"),
        )

    @Test
    fun acceptsValidCorpus() {
        val kb =
            KbValidator.validate(
                archetypes = listOf(goodArchetype()),
                species = listOf(goodSpecies()),
            )
        assertThat(kb.archetypes).hasSize(1)
        assertThat(kb.species).hasSize(1)
    }

    @Test
    fun rejectsUnknownArchetypeId() {
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype()),
                    species = listOf(goodSpecies(archetypeId = "no-such")),
                )
            }
        assertThat(ex.message).contains("no-such")
        assertThat(ex.message).contains("ficus-lyrata")
    }

    @Test
    fun rejectsRecipeNotSummingTo100() {
        val bad =
            goodArchetype().copy(
                recipe =
                    listOf(
                        RecipeIngredient("coir", 59),
                        RecipeIngredient("perlite", 30),
                        RecipeIngredient("bark fines", 10),
                    ),
            )
        val ex =
            expectFailure {
                KbValidator.validate(archetypes = listOf(bad), species = emptyList())
            }
        assertThat(ex.message).contains("standard")
        assertThat(ex.message).contains("99")
    }

    @Test
    fun rejectsMissingSpeciesPlaceholderInRationaleTemplate() {
        val bad = goodArchetype().copy(rationaleTemplate = "Standard mix suits houseplants.")
        val ex =
            expectFailure {
                KbValidator.validate(archetypes = listOf(bad), species = emptyList())
            }
        assertThat(ex.message).contains("standard")
        assertThat(ex.message).contains("{species}")
    }

    @Test
    fun rejectsDuplicateArchetypeId() {
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype("dup"), goodArchetype("dup")),
                    species = emptyList(),
                )
            }
        assertThat(ex.message).contains("duplicate archetype id")
        assertThat(ex.message).contains("dup")
    }

    @Test
    fun rejectsBlendWithPrimaryPctZero() {
        val species =
            goodSpecies(id = "hoya").copy(
                mapping =
                    ArchetypeMapping.Blend(
                        primaryArchetypeId = "standard",
                        secondaryArchetypeId = "aroid",
                        primaryPct = 0,
                    ),
            )
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype("standard"), goodArchetype("aroid")),
                    species = listOf(species),
                )
            }
        assertThat(ex.message).contains("hoya")
        assertThat(ex.message).contains("0")
    }

    @Test
    fun rejectsBlendWithPrimaryPctHundred() {
        val species =
            goodSpecies(id = "hoya").copy(
                mapping =
                    ArchetypeMapping.Blend(
                        primaryArchetypeId = "standard",
                        secondaryArchetypeId = "aroid",
                        primaryPct = 100,
                    ),
            )
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype("standard"), goodArchetype("aroid")),
                    species = listOf(species),
                )
            }
        assertThat(ex.message).contains("hoya")
        assertThat(ex.message).contains("100")
    }

    @Test
    fun rejectsDuplicateNormalisedAlias() {
        val a =
            goodSpecies(
                id = "snake-plant",
                scientificName = "Dracaena trifasciata",
                aliases = listOf("Sansevieria Trifasciata"),
            )
        val b =
            goodSpecies(
                id = "snake-plant-clone",
                scientificName = "Sansevieria trifasciata",
            )
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype()),
                    species = listOf(a, b),
                )
            }
        assertThat(ex.message).contains("duplicate normalised alias")
        assertThat(ex.message).contains("sansevieria trifasciata")
    }

    @Test
    fun rejectsArchetypeWithEmptyCitations() {
        val bad = goodArchetype().copy(citations = emptyList())
        val ex =
            expectFailure {
                KbValidator.validate(archetypes = listOf(bad), species = emptyList())
            }
        assertThat(ex.message).contains("citations array is empty")
        assertThat(ex.message).contains("standard")
    }

    @Test
    fun rejectsSpeciesWithEmptyCitations() {
        val bad = goodSpecies().copy(citations = emptyList())
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype()),
                    species = listOf(bad),
                )
            }
        assertThat(ex.message).contains("citations array is empty")
        assertThat(ex.message).contains("ficus-lyrata")
    }

    @Test
    fun rejectsRationaleWithTodoToken() {
        val bad = goodArchetype().copy(rationaleTemplate = "TODO support {species}")
        val ex =
            expectFailure {
                KbValidator.validate(archetypes = listOf(bad), species = emptyList())
            }
        assertThat(ex.message).contains("standard")
        assertThat(ex.message!!.lowercase()).contains("placeholder token")
    }

    @Test
    fun rejectsSpeciesRationaleWithStubToken() {
        val bad = goodSpecies().copy(speciesRationale = "Stub rationale.")
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype()),
                    species = listOf(bad),
                )
            }
        assertThat(ex.message).contains("ficus-lyrata")
        assertThat(ex.message!!.lowercase()).contains("placeholder token")
    }

    @Test
    fun rejectsRationaleWithLoremToken() {
        val bad =
            goodArchetype().copy(
                rationaleTemplate = "Lorem ipsum suits {species}.",
            )
        val ex =
            expectFailure {
                KbValidator.validate(archetypes = listOf(bad), species = emptyList())
            }
        assertThat(ex.message!!.lowercase()).contains("placeholder token")
    }

    @Test
    fun rejectsRationaleWithPlaceholderToken() {
        val bad =
            goodArchetype().copy(
                rationaleTemplate = "Placeholder text for {species}",
            )
        val ex =
            expectFailure {
                KbValidator.validate(archetypes = listOf(bad), species = emptyList())
            }
        assertThat(ex.message!!.lowercase()).contains("placeholder token")
    }

    @Test
    fun rejectsDuplicateSpeciesId() {
        val ex =
            expectFailure {
                KbValidator.validate(
                    archetypes = listOf(goodArchetype()),
                    species = listOf(goodSpecies(id = "dup"), goodSpecies(id = "dup")),
                )
            }
        assertThat(ex.message).contains("duplicate species id")
        assertThat(ex.message).contains("dup")
    }

    @Test
    fun resolvesAliasesCaseInsensitively() {
        val kb =
            KbValidator.validate(
                archetypes = listOf(goodArchetype()),
                species =
                    listOf(
                        goodSpecies(
                            id = "snake-plant",
                            scientificName = "Dracaena trifasciata",
                            aliases = listOf("Sansevieria trifasciata"),
                        ),
                    ),
            )
        assertThat(kb.findSpecies("SANSEVIERIA TRIFASCIATA")?.id).isEqualTo("snake-plant")
        assertThat(kb.findSpecies("Dracaena Trifasciata")?.id).isEqualTo("snake-plant")
    }

    private fun expectFailure(block: () -> Unit): KbValidationException {
        try {
            block()
        } catch (e: KbValidationException) {
            return e
        }
        throw AssertionError("expected KbValidationException, none thrown")
    }
}
