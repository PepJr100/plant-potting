package com.darkfactory.plantpotting.persistence

import androidx.datastore.core.DataStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * PLANTPOTTING-0010 Phase 1 — logic coverage for [DataStorePlantLogStore]: append/read round-trip,
 * request-counter increment + total, N-cap eviction, fake-clock timestamps, empty-store cold read.
 *
 * These exercise the store against an in-memory [DataStore] (no host file I/O — DataStore's
 * atomic write-then-rename is flaky on the Windows host JVM, though correct on Android). The
 * on-disk encode/decode is covered separately by [PlantLogSerializerTest]; the file-backed
 * survives-restart round-trip is an instrumented test (My Plants, Phase 4).
 */
class DataStorePlantLogStoreTest {
    /** Minimal in-memory [DataStore] for unit tests. */
    private class InMemoryDataStore(initial: PlantLogDocument = PlantLogDocument()) :
        DataStore<PlantLogDocument> {
        private val state = MutableStateFlow(initial)
        override val data: Flow<PlantLogDocument> = state

        override suspend fun updateData(
            transform: suspend (t: PlantLogDocument) -> PlantLogDocument,
        ): PlantLogDocument {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }

    private fun store(
        clock: TimeProvider = TimeProvider { 1_000L },
        max: Int = 100,
    ): DataStorePlantLogStore = DataStorePlantLogStore(InMemoryDataStore(), clock, max)

    @Test
    fun emptyStoreColdReadReturnsEmptyCollections() =
        runBlocking {
            val s = store()
            assertThat(s.identifiedPlants.first()).isEmpty()
            assertThat(s.addPlantRequests.first()).isEmpty()
            assertThat(s.snapshot()).isEqualTo(PlantLogDocument())
        }

    @Test
    fun saveIdentifiedPlantRoundTripsMostRecentFirstWithFakeClock() =
        runBlocking {
            val s = store(clock = TimeProvider { 4_242L })
            s.saveIdentifiedPlant("monstera-deliciosa", "Swiss cheese plant", "ON_DEVICE_MODEL", 90)
            s.saveIdentifiedPlant("aloe-vera", "Aloe", "ON_DEVICE_MODEL", null)

            val plants = s.identifiedPlants.first()
            assertThat(plants).hasSize(2)
            // Most-recent first.
            assertThat(plants[0].speciesId).isEqualTo("aloe-vera")
            assertThat(plants[1].speciesId).isEqualTo("monstera-deliciosa")
            // Fake-clock timestamp + fields preserved.
            assertThat(plants[0].savedAtEpochMs).isEqualTo(4_242L)
            assertThat(plants[1].confidencePct).isEqualTo(90)
            assertThat(plants[0].confidencePct).isNull()
        }

    @Test
    fun identifiedPlantsAreCappedToMostRecentN() =
        runBlocking {
            val s = store(max = 3)
            repeat(5) { i -> s.saveIdentifiedPlant("species-$i", "Name $i", "ON_DEVICE_MODEL", i) }
            val plants = s.identifiedPlants.first()
            assertThat(plants).hasSize(3)
            // The three most-recent survive; oldest evicted first.
            assertThat(plants.map { it.speciesId }).containsExactly("species-4", "species-3", "species-2").inOrder()
        }

    @Test
    fun savingSameSpeciesTwiceDeDupesAndRefreshesToFront() =
        runBlocking {
            // Review feedback: the same plant must not be added multiple times.
            var now = 10L
            val s = DataStorePlantLogStore(InMemoryDataStore(), TimeProvider { now }, 100)
            s.saveIdentifiedPlant("aloe-vera", "Aloe", "ON_DEVICE_MODEL", 30)
            s.saveIdentifiedPlant("ficus-elastica", "Rubber plant", "ON_DEVICE_MODEL", 40)
            now = 99L
            // Save aloe-vera again with new data → one row, refreshed + moved to front.
            s.saveIdentifiedPlant("aloe-vera", "Aloe vera", "ON_DEVICE_MODEL", 88)

            val plants = s.identifiedPlants.first()
            assertThat(plants).hasSize(2)
            assertThat(plants.count { it.speciesId == "aloe-vera" }).isEqualTo(1)
            assertThat(plants[0].speciesId).isEqualTo("aloe-vera")
            assertThat(plants[0].displayName).isEqualTo("Aloe vera")
            assertThat(plants[0].confidencePct).isEqualTo(88)
            assertThat(plants[0].savedAtEpochMs).isEqualTo(99L)
        }

    @Test
    fun removeIdentifiedPlantDeletesBySpeciesId() =
        runBlocking {
            val s = store()
            s.saveIdentifiedPlant("aloe-vera", "Aloe", "ON_DEVICE_MODEL", 30)
            s.saveIdentifiedPlant("ficus-elastica", "Rubber plant", "ON_DEVICE_MODEL", 40)

            s.removeIdentifiedPlant("aloe-vera")
            val plants = s.identifiedPlants.first()
            assertThat(plants.map { it.speciesId }).containsExactly("ficus-elastica")

            // Removing an absent id is a no-op.
            s.removeIdentifiedPlant("not-present")
            assertThat(s.identifiedPlants.first()).hasSize(1)
        }

    @Test
    fun recordAddPlantRequestIncrementsAndTotalsPerClass() =
        runBlocking {
            val s = store(clock = TimeProvider { 7L })
            s.recordAddPlantRequest("Chinese Money Plant (Pilea peperomioides)")
            s.recordAddPlantRequest("Chinese Money Plant (Pilea peperomioides)")
            s.recordAddPlantRequest("Tulip")

            val requests = s.addPlantRequests.first()
            assertThat(requests).hasSize(2)
            val pilea = requests.single { it.modelClassLabel.startsWith("Chinese Money") }
            assertThat(pilea.count).isEqualTo(2)
            assertThat(pilea.lastRequestedEpochMs).isEqualTo(7L)
            assertThat(requests.single { it.modelClassLabel == "Tulip" }.count).isEqualTo(1)
        }

    @Test
    fun snapshotReflectsBothCollections() =
        runBlocking {
            val s = store()
            s.saveIdentifiedPlant("ficus-elastica", "Rubber plant", "ON_DEVICE_MODEL", 77)
            s.recordAddPlantRequest("Tulip")
            val snap = s.snapshot()
            assertThat(snap.identifiedPlants.single().speciesId).isEqualTo("ficus-elastica")
            assertThat(snap.addPlantRequests.single().modelClassLabel).isEqualTo("Tulip")
        }
}
