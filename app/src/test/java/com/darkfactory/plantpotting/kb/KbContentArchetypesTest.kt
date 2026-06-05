package com.darkfactory.plantpotting.kb

import com.darkfactory.plantpotting.kb.model.Archetype
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test
import java.io.File

class KbContentArchetypesTest {
    private val archetypes: List<Archetype> by lazy {
        val file = File("src/main/assets/kb/archetypes.json")
        check(file.exists()) {
            "archetypes.json not found at ${file.absolutePath} (cwd ${File(".").absolutePath})"
        }
        KbLoader.DefaultJson.decodeFromString(
            ListSerializer(Archetype.serializer()),
            file.readText(Charsets.UTF_8),
        )
    }

    @Test
    fun bundlesExactlyNineArchetypes() {
        // PLANTPOTTING-0009: +carnivorous-peat-sand (Venus Flytrap) = 9.
        assertThat(archetypes).hasSize(9)
    }

    @Test
    fun carnivorousPeatSandArchetypeIsPresent() {
        // PLANTPOTTING-0009: the only new archetype this sprint.
        assertThat(archetypes.map { it.id }).contains("carnivorous-peat-sand")
    }

    @Test
    fun everyRecipeSumsToOneHundred() {
        for (a in archetypes) {
            val sum = a.recipe.sumOf { it.proportionPct }
            assertWithMessage("archetype ${a.id} recipe sum").that(sum).isEqualTo(100)
        }
    }

    @Test
    fun everyRationaleTemplateMentionsSpeciesPlaceholder() {
        for (a in archetypes) {
            assertWithMessage("archetype ${a.id} rationaleTemplate")
                .that(a.rationaleTemplate)
                .contains("{species}")
        }
    }

    @Test
    fun archetypeIdsAreUnique() {
        val ids = archetypes.map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun everyArchetypeHasAtLeastOneCitation() {
        for (a in archetypes) {
            assertWithMessage("archetype ${a.id} citations").that(a.citations).isNotEmpty()
        }
    }

    @Test
    fun mandatedArchetypeIdsArePresent() {
        val ids = archetypes.map { it.id }.toSet()
        val required =
            setOf(
                "standard-houseplant",
                "aroid-chunky",
                "succulent-gritty",
                "cactus-pure-mineral",
                "epiphytic-orchid-bark",
                "moisture-retentive",
                "semi-hydro-inert",
                "acidic-ericaceous",
            )
        assertThat(ids).containsAtLeastElementsIn(required)
    }
}
