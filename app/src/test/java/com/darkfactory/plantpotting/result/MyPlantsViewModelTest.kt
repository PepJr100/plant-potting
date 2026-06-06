package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyPlantsViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptyStoreYieldsEmptyState() =
        runTest {
            val vm = MyPlantsViewModel(FakePlantLogStore())
            vm.state.test {
                assertThat(awaitItem().isEmpty).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun rowsAreMostRecentFirstWithSourceParsed() =
        runTest {
            val store = FakePlantLogStore(nowEpochMs = 50L)
            store.saveIdentifiedPlant("aloe-vera", "Aloe", IdSource.ON_DEVICE_MODEL.name, 30)
            store.saveIdentifiedPlant("ficus-elastica", "Rubber plant", IdSource.ON_DEVICE_MODEL.name, 70)
            val vm = MyPlantsViewModel(store)
            vm.state.test {
                var rows = awaitItem().rows
                while (rows.isEmpty()) rows = awaitItem().rows
                assertThat(rows.map { it.speciesId }).containsExactly("ficus-elastica", "aloe-vera").inOrder()
                assertThat(rows[0].source).isEqualTo(IdSource.ON_DEVICE_MODEL)
                assertThat(rows[0].confidencePct).isEqualTo(70)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun identifyThenSaveThenRowAppears() =
        runTest {
            // identify → Save (via ResultViewModel) → the row appears in My Plants over the shared store.
            val store = FakePlantLogStore(nowEpochMs = 9L)
            val resultVm =
                ResultViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                Routes.ARG_SPECIES_ID to "ficus-lyrata",
                                Routes.ARG_SOURCE to IdSource.ON_DEVICE_MODEL.name,
                                Routes.ARG_CONFIDENCE_PCT to 91,
                            ),
                        ),
                    kb = kb,
                    plantLogStore = store,
                )
            resultVm.saveToMyPlants()

            val myPlants = MyPlantsViewModel(store)
            myPlants.state.test {
                var rows = awaitItem().rows
                while (rows.isEmpty()) rows = awaitItem().rows
                val row = rows.single()
                assertThat(row.speciesId).isEqualTo("ficus-lyrata")
                assertThat(row.displayName).isEqualTo("Fiddle-leaf fig")
                assertThat(row.confidencePct).isEqualTo(91)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun removeDeletesThePlantFromState() =
        runTest {
            val store = FakePlantLogStore(nowEpochMs = 1L)
            store.saveIdentifiedPlant("aloe-vera", "Aloe", IdSource.ON_DEVICE_MODEL.name, 30)
            store.saveIdentifiedPlant("ficus-elastica", "Rubber plant", IdSource.ON_DEVICE_MODEL.name, 40)
            val vm = MyPlantsViewModel(store)
            vm.state.test {
                var rows = awaitItem().rows
                while (rows.size < 2) rows = awaitItem().rows
                vm.remove("aloe-vera")
                var after = awaitItem().rows
                while (after.any { it.speciesId == "aloe-vera" }) after = awaitItem().rows
                assertThat(after.map { it.speciesId }).containsExactly("ficus-elastica")
                cancelAndIgnoreRemainingEvents()
            }
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
        ).let { it.copy(speciesIndex = it.species.associateBy { s -> s.id }) }
}
