package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.darkfactory.plantpotting.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0003 §6.1 — locks the picker view-model behaviour:
 *  - candidates are decoded from the `speciesId|probPct,...` nav-arg
 *  - manual search filters by common name *and* scientific name
 *  - empty/blank nav-arg yields empty `topCandidates`
 */
class LowConfidencePickerViewModelTest {
    private val kb = fixtureKb()

    private fun vm(candidatesArg: String?): LowConfidencePickerViewModel {
        val saved =
            SavedStateHandle(
                mapOf(Routes.ARG_CANDIDATES to candidatesArg.orEmpty()),
            )
        return LowConfidencePickerViewModel(savedStateHandle = saved, kb = kb)
    }

    @Test
    fun decodesUpToThreeCandidatesFromNavArg() {
        val v = vm("monstera-deliciosa|72,ficus-lyrata|18,dracaena-trifasciata|7")
        assertThat(v.topCandidates).hasSize(3)
        assertThat(v.topCandidates.map { it.speciesId })
            .containsExactly("monstera-deliciosa", "ficus-lyrata", "dracaena-trifasciata")
            .inOrder()
        assertThat(v.topCandidates[0].probabilityPct).isEqualTo(72)
        assertThat(v.topCandidates[1].probabilityPct).isEqualTo(18)
        assertThat(v.topCandidates[2].probabilityPct).isEqualTo(7)
    }

    @Test
    fun emptyNavArgYieldsEmptyTopCandidates() {
        assertThat(vm("").topCandidates).isEmpty()
        assertThat(vm(null).topCandidates).isEmpty()
    }

    @Test
    fun candidatePointingAtUnknownSpeciesIsDropped() {
        val v = vm("monstera-deliciosa|80,not-a-real-species|10")
        assertThat(v.topCandidates.map { it.speciesId }).containsExactly("monstera-deliciosa")
    }

    @Test
    fun malformedCandidateRowIsDropped() {
        val v = vm("monstera-deliciosa|80,bogus-entry-no-pipe,ficus-lyrata|abc")
        assertThat(v.topCandidates.map { it.speciesId }).containsExactly("monstera-deliciosa")
    }

    @Test
    fun searchFilterMatchesCommonName() {
        val v = vm("")
        v.onQueryChange("swiss")
        val filtered = v.filteredSpecies.value
        assertThat(filtered.map { it.id }).contains("monstera-deliciosa")
        assertThat(filtered.size).isLessThan(kb.species.size)
    }

    @Test
    fun searchFilterMatchesScientificName() {
        val v = vm("")
        v.onQueryChange("Ficus")
        val filtered = v.filteredSpecies.value
        assertThat(filtered.map { it.id }).contains("ficus-lyrata")
    }

    @Test
    fun emptyQueryShowsAllSpecies() {
        val v = vm("")
        v.onQueryChange("xyz")
        v.onQueryChange("")
        assertThat(v.filteredSpecies.value).hasSize(kb.species.size)
    }

    @Test
    fun candidatesAreCappedAtThree() {
        val v = vm("monstera-deliciosa|80,ficus-lyrata|10,dracaena-trifasciata|5,hoya-carnosa|3,phalaenopsis|2")
        assertThat(v.topCandidates).hasSize(3)
    }

    // ---- fixtures ----

    private fun fixtureKb(): KnowledgeBase {
        val arch =
            Archetype(
                id = "standard",
                displayName = "Standard",
                shortDescription = "",
                recipe = listOf(RecipeIngredient("coir", 100)),
                rationaleTemplate = "Suits {species}.",
                citations = listOf("Brief"),
            )
        val ids =
            listOf(
                "monstera-deliciosa" to ("Monstera deliciosa" to "Swiss cheese plant"),
                "ficus-lyrata" to ("Ficus lyrata" to "Fiddle-leaf fig"),
                "dracaena-trifasciata" to ("Dracaena trifasciata" to "Snake plant"),
                "hoya-carnosa" to ("Hoya carnosa" to "Wax plant"),
                "phalaenopsis" to ("Phalaenopsis" to "Moth orchid"),
            )
        val list =
            ids.map { (id, names) ->
                Species(
                    id = id,
                    scientificName = names.first,
                    commonNames = listOf(names.second),
                    aliases = emptyList(),
                    mapping = ArchetypeMapping.Single("standard"),
                    speciesRationale = "${names.first}.",
                    citations = listOf("Brief"),
                )
            }
        return KnowledgeBase(
            archetypes = mapOf("standard" to arch),
            species = list,
            speciesIndex = list.associateBy { it.id },
        )
    }
}
