package com.darkfactory.plantpotting.kb

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test
import java.io.File

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
    fun bundlesExactlyThirtyTwoSpecies() {
        // PLANTPOTTING-0009: 16 original + 16 delta species = 32.
        assertThat(species).hasSize(32)
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
                    assertWithMessage("species ${s.id} archetypeId").that(archetypeIds).contains(m.archetypeId)
                }
                is ArchetypeMapping.Blend -> {
                    assertWithMessage("species ${s.id} primaryArchetypeId")
                        .that(archetypeIds)
                        .contains(m.primaryArchetypeId)
                    assertWithMessage("species ${s.id} secondaryArchetypeId")
                        .that(archetypeIds)
                        .contains(m.secondaryArchetypeId)
                }
            }
        }
    }

    @Test
    fun everySpeciesRationaleIsNonEmpty() {
        for (s in species) {
            assertWithMessage("species ${s.id} rationale").that(s.speciesRationale.trim()).isNotEmpty()
        }
    }

    @Test
    fun everySpeciesHasAtLeastOneCitation() {
        for (s in species) {
            assertWithMessage("species ${s.id} citations").that(s.citations).isNotEmpty()
        }
    }

    @Test
    fun normalisedAliasesAreUnique() {
        val seen = mutableMapOf<String, String>()
        for (s in species) {
            for (alias in s.aliases + s.scientificName) {
                val key = KnowledgeBase.normalise(alias)
                val prior = seen.put(key, s.id)
                assertWithMessage("alias $key already owned by $prior").that(prior).isNull()
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
    fun dionaeaMuscipulaMapsToCarnivorousPeatSand() {
        // PLANTPOTTING-0009: Venus Flytrap is the only species needing the new archetype.
        val dionaea = species.single { it.id == "dionaea-muscipula" }
        assertThat(dionaea.mapping).isInstanceOf(ArchetypeMapping.Single::class.java)
        val single = dionaea.mapping as ArchetypeMapping.Single
        assertThat(single.archetypeId).isEqualTo("carnivorous-peat-sand")
        assertThat(archetypeIds).contains("carnivorous-peat-sand")
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
