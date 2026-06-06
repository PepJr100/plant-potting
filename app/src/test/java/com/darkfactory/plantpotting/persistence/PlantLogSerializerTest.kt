package com.darkfactory.plantpotting.persistence

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PlantLogSerializerTest {
    @Test
    fun emptyStreamRecoversToDefaultValueNotCorruption() =
        runBlocking {
            val doc = PlantLogSerializer.readFrom(ByteArrayInputStream(ByteArray(0)))
            assertThat(doc).isEqualTo(PlantLogDocument())
        }

    @Test
    fun writeThenReadRoundTrips() =
        runBlocking {
            val original =
                PlantLogDocument(
                    identifiedPlants = listOf(IdentifiedPlant("aloe-vera", "Aloe", "ON_DEVICE_MODEL", 80, 5L)),
                    addPlantRequests = listOf(AddPlantRequest("Tulip", 2, 9L)),
                )
            val out = ByteArrayOutputStream()
            PlantLogSerializer.writeTo(original, out)
            val restored = PlantLogSerializer.readFrom(ByteArrayInputStream(out.toByteArray()))
            assertThat(restored).isEqualTo(original)
        }

    @Test(expected = CorruptionException::class)
    fun malformedJsonThrowsCorruptionException() =
        runBlocking {
            PlantLogSerializer.readFrom(ByteArrayInputStream("{ not valid json".toByteArray()))
            Unit
        }
}
