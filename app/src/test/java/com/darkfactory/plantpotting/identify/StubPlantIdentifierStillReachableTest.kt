package com.darkfactory.plantpotting.identify

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * PLANTPOTTING-0003 §4.4 — proves `StubPlantIdentifier` still compiles, instantiates, and
 * returns a valid `IdentificationResult` after the Phase 4 Hilt swap removed its
 * *production* binding. The class itself stays inside `identify/` so the §4.5 stub-isolation
 * grep stays clean, and so test-only consumers (`StubPlantIdentifierTest`,
 * instrumentation modules) can still depend on it.
 */
class StubPlantIdentifierStillReachableTest {
    @Test
    fun stubPlantIdentifierConstructsAndIdentifiesFromKb() =
        runBlocking {
            val archetype =
                Archetype(
                    id = "standard",
                    displayName = "Standard",
                    shortDescription = "",
                    recipe = listOf(RecipeIngredient("coir", 100)),
                    rationaleTemplate = "Suits {species}.",
                    citations = listOf("Brief"),
                )
            val monstera =
                Species(
                    id = "monstera-deliciosa",
                    scientificName = "Monstera deliciosa",
                    commonNames = listOf("Swiss cheese plant"),
                    aliases = emptyList(),
                    mapping = ArchetypeMapping.Single("standard"),
                    speciesRationale = "M. deliciosa.",
                    citations = listOf("Brief"),
                )
            val kb =
                KnowledgeBase(
                    archetypes = mapOf("standard" to archetype),
                    species = listOf(monstera),
                    speciesIndex = mapOf(monstera.id to monstera),
                )
            val identifier = StubPlantIdentifier(kb = kb)
            val result = identifier.identify(byteArrayOf(0))
            assertThat(result.speciesId).isEqualTo("monstera-deliciosa")
            assertThat(result.source).isEqualTo(IdSource.STUB_DETERMINISTIC)
            // After §3.1 the additive field defaults to false for stub paths.
            assertThat(result.lowConfidence).isFalse()
        }
}
