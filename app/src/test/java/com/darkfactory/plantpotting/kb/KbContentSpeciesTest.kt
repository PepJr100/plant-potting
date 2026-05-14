package com.darkfactory.plantpotting.kb

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test

class KbContentSpeciesTest {
    private val json = KbLoader.DefaultJson

    private val archetypes: List<Archetype> by lazy {
        val file = File("src/main/assets/kb/archetypes.json")
        check(file.exists()) { "archetypes.json not found at ${file.absolutePath}" }
        json.decodeFromString(ListSerializer(Archetype.serializer()), file.readText(Charsets.UTF_8))
    }

    private val species: List<Species> by lazy {
        val file = File("src/main/assets/kb/species.json")
        check(file.exists()) { "species.json not found at ${file.absolutePath}" }
        json.decodeFromString(ListSerializer(Species.serializer()), file.readText(Charsets.UTF_8))
    }

    private val archetypeIds: Set<String> by lazy { archetypes.map { it.id }.toSet() }

    @Test
    fun bundlesExactlySixteenSpecies() {
        assertThat(species).hasSize(16)
    }

    @Test
    fun speciesIdsAreUnique() {
        assertThat(species.map { it.id }).containsNoDuplicates()
    }

    @Test
    fun everyMappingResolvesToKnownArchetype() {
        for (s in species) {
            when (val m = s.mapping) {
                is ArchetypeMapping.Single -> {
                    assertThat(archetypeIds).named("species ${s.id} archetypeId").contains(m.archetypeId)
                }
                is ArchetypeMapping.Blend -> {
                    assertThat(archetypeIds)
                        .named("species ${s.id} primaryArchetypeId")
                        .contains(m.primaryArchetypeId)
                    assertThat(archetypeIds)
                        .named("species ${s.id} secondaryArchetypeId")
                        .contains(m.secondaryArchetypeId)
                }
            }
        }
    }

    @Test
    fun everySpeciesRationaleIsNonEmpty() {
        for (s in species) {
            assertThat(s.speciesRationale.trim()).named("species ${s.id} rationale").isNotEmpty()
        }
    }

    @Test
    fun everySpeciesHasAtLeastOneCitation() {
        for (s in species) {
            assertThat(s.citations).named("species ${s.id} citations").isNotEmpty()
        }
    }

    @Test
    fun normalisedAliasesAreUnique() {
        val seen = mutableMapOf<String, String>()
        for (s in species) {
            for (alias in s.aliases + s.scientificName) {
                val key = KnowledgeBase.normalise(alias)
                val prior = seen.put(key, s.id)
                assertThat(prior).named("alias $key already owned by $prior").isNull()
            }
        }
    }

    @Test
    fun sansevieriaTrifasciataAliasesToDracaenaTrifasciata() {
        val kb = KbValidator.validate(archetypes, species)
        val found = kb.findSpecies("Sansevieria trifasciata")
        assertThat(found).isNotNull()
        assertThat(found!!.scientificName).isEqualTo("Dracaena trifasciata")
    }

    @Test
    fun calatheaOrbifoliaAliasesToGoeppertiaOrbifolia() {
        val kb = KbValidator.validate(archetypes, species)
        val found = kb.findSpecies("Calathea orbifolia")
        assertThat(found).isNotNull()
        assertThat(found!!.scientificName).isEqualTo("Goeppertia orbifolia")
    }

    @Test
    fun hoyaCarnosaIsBlendMapped() {
        val hoya = species.single { it.scientificName == "Hoya carnosa" }
        assertThat(hoya.mapping).isInstanceOf(ArchetypeMapping.Blend::class.java)
        val blend = hoya.mapping as ArchetypeMapping.Blend
        assertThat(blend.primaryArchetypeId).isEqualTo("aroid-chunky")
        assertThat(blend.secondaryArchetypeId).isEqualTo("succulent-gritty")
        assertThat(blend.primaryPct).isEqualTo(60)
    }
}
