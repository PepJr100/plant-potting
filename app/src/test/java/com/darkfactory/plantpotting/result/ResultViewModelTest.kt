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

class ResultViewModelTest {
    private val kb =
        KnowledgeBase(
            archetypes =
                mapOf(
                    "standard" to
                        Archetype(
                            id = "standard",
                            displayName = "Standard",
                            shortDescription = "",
                            recipe = listOf(RecipeIngredient("coir", 100)),
                            rationaleTemplate = "Suits {species}.",
                            citations = listOf("Brief"),
                        ),
                ),
            species =
                listOf(
                    Species(
                        id = "ficus-lyrata",
                        scientificName = "Ficus lyrata",
                        commonNames = listOf("Fiddle-leaf fig"),
                        aliases = emptyList(),
                        mapping = ArchetypeMapping.Single("standard"),
                        speciesRationale = "Ficus lyrata is a fig.",
                        citations = listOf("Brief"),
                    ),
                ),
            speciesIndex = emptyMap(),
        ).let { kb ->
            kb.copy(speciesIndex = kb.species.associateBy { it.id })
        }

    @Test
    fun knownIdPopulatesScientificAndCommonName() {
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "ficus-lyrata"))
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb)
        val state = vm.state.value
        assertThat(state.notFound).isFalse()
        assertThat(state.scientificName).isEqualTo("Ficus lyrata")
        assertThat(state.commonName).isEqualTo("Fiddle-leaf fig")
        assertThat(state.speciesId).isEqualTo("ficus-lyrata")
    }

    @Test
    fun unknownIdMarksNotFound() {
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "ghost-plant"))
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb)
        assertThat(vm.state.value.notFound).isTrue()
    }
}
