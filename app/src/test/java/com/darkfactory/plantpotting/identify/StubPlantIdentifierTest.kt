package com.darkfactory.plantpotting.identify

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Test

class StubPlantIdentifierTest {

    private val archetype = Archetype(
        id = "standard",
        displayName = "Standard",
        shortDescription = "",
        recipe = listOf(RecipeIngredient("coir", 100)),
        rationaleTemplate = "Suits {species}.",
        citations = listOf("Brief"),
    )

    private fun species(id: String, scientificName: String, common: String = "common"): Species =
        Species(
            id = id,
            scientificName = scientificName,
            commonNames = listOf(common),
            aliases = emptyList(),
            mapping = ArchetypeMapping.Single("standard"),
            speciesRationale = "$scientificName.",
            citations = listOf("Brief"),
        )

    private fun kb(species: List<Species>): KnowledgeBase = KnowledgeBase(
        archetypes = mapOf("standard" to archetype),
        species = species,
        speciesIndex = species.associateBy { it.id },
    )

    private val speciesList = listOf(
        species("monstera-deliciosa", "Monstera deliciosa"),
        species("ficus-lyrata", "Ficus lyrata"),
        species("dracaena-trifasciata", "Dracaena trifasciata"),
    )

    @Test
    fun deterministicReturnsStableResultAcrossRuns() = runBlocking {
        val a = StubPlantIdentifier(kb = kb(speciesList))
        val b = StubPlantIdentifier(kb = kb(speciesList))
        val r1 = a.identify(ByteArray(0))
        val r2 = b.identify(ByteArray(0))
        assertThat(r1.speciesId).isEqualTo(r2.speciesId)
    }

    @Test
    fun deterministicResultIsAlwaysOneOfKbSpecies() = runBlocking {
        val identifier = StubPlantIdentifier(kb = kb(speciesList))
        val result = identifier.identify(ByteArray(0))
        val knownIds = speciesList.map { it.id }
        assertThat(knownIds).contains(result.speciesId)
    }

    @Test
    fun deterministicSourceIsStubDeterministic() = runBlocking {
        val identifier = StubPlantIdentifier(kb = kb(speciesList))
        val result = identifier.identify(ByteArray(0))
        assertThat(result.source).isEqualTo(IdSource.STUB_DETERMINISTIC)
    }

    @Test
    fun randomModeOnlyReturnsSpeciesPresentInKb() = runBlocking {
        val identifier = StubPlantIdentifier(
            kb = kb(speciesList),
            random = Random(42L),
            mode = StubPlantIdentifier.Mode.RANDOM,
        )
        val knownIds = speciesList.map { it.id }.toSet()
        repeat(50) {
            val result = identifier.identify(ByteArray(0))
            assertThat(knownIds).contains(result.speciesId)
            assertThat(result.source).isEqualTo(IdSource.STUB_RANDOM)
        }
    }

    @Test
    fun deterministicPicksMonsteraIfPresent() = runBlocking {
        val identifier = StubPlantIdentifier(kb = kb(speciesList))
        val result = identifier.identify(ByteArray(0))
        assertThat(result.speciesId).isEqualTo("monstera-deliciosa")
    }

    @Test
    fun deterministicFallsBackToFirstIfNoMonstera() = runBlocking {
        val without = speciesList.filterNot { it.scientificName == "Monstera deliciosa" }
        val identifier = StubPlantIdentifier(kb = kb(without))
        val result = identifier.identify(ByteArray(0))
        assertThat(result.speciesId).isEqualTo(without.first().id)
    }
}
