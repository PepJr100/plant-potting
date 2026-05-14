package com.darkfactory.plantpotting.kb

import com.darkfactory.plantpotting.kb.model.Archetype
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test

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
    fun bundlesExactlyEightArchetypes() {
        assertThat(archetypes).hasSize(8)
    }

    @Test
    fun everyRecipeSumsToOneHundred() {
        for (a in archetypes) {
            val sum = a.recipe.sumOf { it.proportionPct }
            assertThat(sum).named("archetype ${a.id} recipe sum").isEqualTo(100)
        }
    }

    @Test
    fun everyRationaleTemplateMentionsSpeciesPlaceholder() {
        for (a in archetypes) {
            assertThat(a.rationaleTemplate)
                .named("archetype ${a.id} rationaleTemplate")
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
            assertThat(a.citations).named("archetype ${a.id} citations").isNotEmpty()
        }
    }

    @Test
    fun mandatedArchetypeIdsArePresent() {
        val ids = archetypes.map { it.id }.toSet()
        val required = setOf(
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
