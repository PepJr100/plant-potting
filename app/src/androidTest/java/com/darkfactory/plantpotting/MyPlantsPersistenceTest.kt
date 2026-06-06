package com.darkfactory.plantpotting

import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.persistence.PlantLogStore
import com.darkfactory.plantpotting.result.MyPlantsViewModel
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * PLANTPOTTING-0010 Phase 4 — round-trips a saved plant through the REAL DataStore-backed
 * [PlantLogStore] on-device (where DataStore's atomic write-then-rename works, unlike the Windows
 * host JVM). Saving writes to the app's on-disk DataStore file; re-reading the flow returns it,
 * and a freshly-constructed [MyPlantsViewModel] surfaces it as a row — the persistence + VM wiring
 * the unit tests cannot exercise against a real file.
 */
@HiltAndroidTest
class MyPlantsPersistenceTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var store: PlantLogStore

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun savedPlantPersistsToDiskAndSurfacesInMyPlants() =
        runBlocking {
            // Unique id so the assertion is robust to entries left by other runs.
            val uniqueId = "ficus-elastica-${System.nanoTime()}"
            store.saveIdentifiedPlant(uniqueId, "Rubber plant", IdSource.ON_DEVICE_MODEL.name, 70)

            // Disk-backed flow returns the saved entry.
            val persisted = store.identifiedPlants.first()
            assertThat(persisted.any { it.speciesId == uniqueId }).isTrue()
            val saved = persisted.first { it.speciesId == uniqueId }
            assertThat(saved.displayName).isEqualTo("Rubber plant")
            assertThat(saved.confidencePct).isEqualTo(70)

            // A fresh MyPlantsViewModel over the same store surfaces it as a row.
            val vm = MyPlantsViewModel(store)
            val rows = vm.state.first { state -> state.rows.any { it.speciesId == uniqueId } }.rows
            assertThat(rows.first { it.speciesId == uniqueId }.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
        }
}
