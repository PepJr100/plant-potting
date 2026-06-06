package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.darkfactory.plantpotting.persistence.FakePlantLogStore
import com.darkfactory.plantpotting.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {
    @Before
    fun setUpDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

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
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = FakePlantLogStore())
        val state = vm.state.value
        assertThat(state.notFound).isFalse()
        assertThat(state.scientificName).isEqualTo("Ficus lyrata")
        assertThat(state.commonName).isEqualTo("Fiddle-leaf fig")
        assertThat(state.speciesId).isEqualTo("ficus-lyrata")
    }

    @Test
    fun unknownIdMarksNotFound() {
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "ghost-plant"))
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = FakePlantLogStore())
        assertThat(vm.state.value.notFound).isTrue()
    }

    // -- PLANTPOTTING-0010 D2 — confidence nav arg ----------------------------

    @Test
    fun confidencePctArgIsParsedIntoState() {
        val saved =
            SavedStateHandle(
                mapOf(Routes.ARG_SPECIES_ID to "ficus-lyrata", Routes.ARG_CONFIDENCE_PCT to 87),
            )
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = FakePlantLogStore())
        assertThat(vm.state.value.confidencePct).isEqualTo(87)
    }

    @Test
    fun absentConfidenceSentinelMapsToNull() {
        // The CONFIDENCE_ABSENT sentinel (-1) must degrade to null, not render as "-1%".
        val saved =
            SavedStateHandle(
                mapOf(
                    Routes.ARG_SPECIES_ID to "ficus-lyrata",
                    Routes.ARG_CONFIDENCE_PCT to Routes.CONFIDENCE_ABSENT,
                ),
            )
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = FakePlantLogStore())
        assertThat(vm.state.value.confidencePct).isNull()
    }

    @Test
    fun missingConfidenceArgDefaultsToNull() {
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "ficus-lyrata"))
        val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = FakePlantLogStore())
        assertThat(vm.state.value.confidencePct).isNull()
    }

    // -- PLANTPOTTING-0010 Phase 4 — Save to My Plants ------------------------

    @Test
    fun saveToMyPlantsPersistsAndSetsSavedFlag() =
        kotlinx.coroutines.test.runTest {
            val store = FakePlantLogStore(nowEpochMs = 123L)
            val saved =
                SavedStateHandle(
                    mapOf(
                        Routes.ARG_SPECIES_ID to "ficus-lyrata",
                        Routes.ARG_SOURCE to IdSource.ON_DEVICE_MODEL.name,
                        Routes.ARG_CONFIDENCE_PCT to 88,
                    ),
                )
            val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = store)

            vm.saveToMyPlants()

            assertThat(vm.state.value.saved).isTrue()
            val stored = store.snapshot().identifiedPlants.single()
            assertThat(stored.speciesId).isEqualTo("ficus-lyrata")
            assertThat(stored.displayName).isEqualTo("Fiddle-leaf fig")
            assertThat(stored.source).isEqualTo("ON_DEVICE_MODEL")
            assertThat(stored.confidencePct).isEqualTo(88)
            assertThat(stored.savedAtEpochMs).isEqualTo(123L)
        }

    @Test
    fun saveToMyPlantsIsNoOpWhenNotFound() =
        kotlinx.coroutines.test.runTest {
            val store = FakePlantLogStore()
            val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "ghost-plant"))
            val vm = ResultViewModel(savedStateHandle = saved, kb = kb, plantLogStore = store)

            vm.saveToMyPlants()

            assertThat(vm.state.value.saved).isFalse()
            assertThat(store.snapshot().identifiedPlants).isEmpty()
        }
}
