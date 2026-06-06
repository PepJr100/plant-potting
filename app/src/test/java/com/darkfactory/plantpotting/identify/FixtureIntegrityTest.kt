package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.kb.KbLoader
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * PLANTPOTTING-0011 Phase 1a — integrity gate for the eval fixtures. For every
 * `identify-fixtures` JPEG: it parses the JPEG header (valid SOI + nonzero SOF dimensions), confirms the
 * expected `<kb-species-id>` (the filename stem before any `__NN` suffix) resolves in the bundled
 * KB, and marks whether that species is reachable by the production `plant_class_map.json`
 * (in-vocab vs not) — logged so the scorecard's in-vocab cut is auditable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class FixtureIntegrityTest {
    private val fixturesDir = File("src/androidTest/assets/identify-fixtures")
    private val productionMappingPath = "ml/house_plant_species_mobilenetv2/plant_class_map.json"
    private val json = Json { ignoreUnknownKeys = true }

    private fun jpgFixtures(): List<File> =
        fixturesDir
            .listFiles { f -> f.isFile && f.name.endsWith(".jpg", ignoreCase = true) }
            ?.sortedBy { it.name }
            ?: emptyList()

    private fun expectedId(file: File): String =
        file.name.removeSuffix(".jpg").removeSuffix(".JPG").substringBefore("__")

    private fun reachableKbIds(context: Context): Set<String> {
        val raw = context.assets.open(productionMappingPath).use { String(it.readBytes(), Charsets.UTF_8) }
        val mapping = json.parseToJsonElement(raw).jsonObject["mapping"]!!.jsonObject
        return mapping.values.map { it.jsonObject["kbSpeciesId"]!!.jsonPrimitive.content }.toSet()
    }

    @Test
    fun fixturesDirectoryIsNotEmpty() {
        assertThat(jpgFixtures()).isNotEmpty()
    }

    @Test
    fun everyFixtureDecodesToNonzeroDimensions() {
        for (file in jpgFixtures()) {
            val dims = jpegDimensions(file)
            assertWithMessage("fixture '${file.name}' must be a valid JPEG with a SOF frame")
                .that(dims)
                .isNotNull()
            assertThat(dims!!.first).isGreaterThan(0)
            assertThat(dims.second).isGreaterThan(0)
        }
    }

    /**
     * Minimal pure-JVM JPEG dimension reader (no `javax.imageio`, absent from the Android unit-test
     * classpath). Validates the SOI marker, then walks segments to the first Start-Of-Frame marker
     * and reads its width/height. Returns null for a non-JPEG or a file with no SOF frame.
     */
    private fun jpegDimensions(file: File): Pair<Int, Int>? {
        val b = file.readBytes()
        if (b.size < 4 || (b[0].toInt() and 0xFF) != 0xFF || (b[1].toInt() and 0xFF) != 0xD8) return null
        var i = 2
        while (i + 1 < b.size) {
            if ((b[i].toInt() and 0xFF) != 0xFF) {
                i++
                continue
            }
            var marker = b[i + 1].toInt() and 0xFF
            i += 2
            while (marker == 0xFF && i < b.size) {
                marker = b[i].toInt() and 0xFF
                i++
            }
            if (marker == 0xD8 || marker == 0xD9 || marker in 0xD0..0xD7) continue // no length payload
            if (i + 1 >= b.size) break
            val len = ((b[i].toInt() and 0xFF) shl 8) or (b[i + 1].toInt() and 0xFF)
            // SOF markers C0..CF except DHT(C4), JPG(C8), DAC(CC).
            if (marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                if (i + 6 >= b.size) break
                val height = ((b[i + 3].toInt() and 0xFF) shl 8) or (b[i + 4].toInt() and 0xFF)
                val width = ((b[i + 5].toInt() and 0xFF) shl 8) or (b[i + 6].toInt() and 0xFF)
                return width to height
            }
            i += len
        }
        return null
    }

    @Test
    fun everyExpectedSpeciesIdResolvesInKb() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val kbIds = KbLoader(context.assets).load().species.map { it.id }.toSet()
            for (file in jpgFixtures()) {
                val id = expectedId(file)
                assertWithMessage("fixture '${file.name}' expected id '$id' must resolve in KB")
                    .that(kbIds.contains(id))
                    .isTrue()
            }
        }

    @Test
    fun inVocabReachabilityIsComputableAndLogged() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val reachable = reachableKbIds(context)
        val inVocab = mutableListOf<String>()
        val outVocab = mutableListOf<String>()
        for (file in jpgFixtures()) {
            val id = expectedId(file)
            if (id in reachable) inVocab += file.name else outVocab += file.name
        }
        println("PLANTPOTTING-0011 fixture in-vocab: $inVocab")
        println("PLANTPOTTING-0011 fixture out-of-vocab: $outVocab")
        // Sanity: the priority failure species (snake plant, pothos) are in-vocab in the production map.
        assertThat(reachable).containsAtLeast("dracaena-trifasciata", "epipremnum-aureum")
        assertThat(inVocab).isNotEmpty()
    }
}
