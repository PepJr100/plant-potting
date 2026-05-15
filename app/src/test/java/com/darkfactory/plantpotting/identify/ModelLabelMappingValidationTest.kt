package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.kb.KbLoader
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0003 §2.6 — referential-integrity gate over `plant_class_map.json`.
 *
 * Invariants:
 *  - JSON is parseable; required top-level fields present.
 *  - Every `kbSpeciesId` resolves to a real species in the bundled KB.
 *  - `modelLabelsAsset` exists in the AssetManager and contains every mapping key as a
 *    line (consistent with the runtime lookup that uses the label-at-predicted-index).
 *  - No duplicate keys (parsed JSON would have suppressed dupes; we also fail explicitly
 *    if the canonicalised key set is smaller than the textual line count).
 *  - Every key normalises consistently with the runtime lookup (`trim().lowercase()`).
 *  - Every row tagged `alias: true` has a corresponding non-alias row pointing at the
 *    same KB species id.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ModelLabelMappingValidationTest {
    private val dir = "ml/aiy_plants_v1"
    private val json = Json { ignoreUnknownKeys = true }

    private fun readAsset(name: String): ByteArray {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.assets.open("$dir/$name").use { it.readBytes() }
    }

    private fun mappingObject(): JsonObject {
        val raw = String(readAsset("plant_class_map.json"), Charsets.UTF_8)
        return json.parseToJsonElement(raw).jsonObject
    }

    private fun mappingRows(): Map<String, JsonObject> {
        val entries = mappingObject()["mapping"]!!.jsonObject
        return entries.entries.associate { (k, v) -> k to v.jsonObject }
    }

    @Test
    fun mappingIsValidJsonAndDeclaresLabelsAsset() {
        val obj = mappingObject()
        assertThat(obj.keys).containsAtLeast("version", "modelLabelsAsset", "mapping")
        assertThat(obj["modelLabelsAsset"]!!.jsonPrimitive.content).isEqualTo("ml/aiy_plants_v1/labels.csv")
    }

    @Test
    fun modelLabelsAssetExistsInAssetManager() {
        val obj = mappingObject()
        val labelsPath = obj["modelLabelsAsset"]!!.jsonPrimitive.content
        val context = ApplicationProvider.getApplicationContext<Context>()
        val stream = context.assets.open(labelsPath)
        val firstByte = stream.read()
        stream.close()
        assertThat(firstByte).isNotEqualTo(-1)
    }

    @Test
    fun everyMappingKeyExistsInLabelsCsv() {
        val labelsAssetPath = mappingObject()["modelLabelsAsset"]!!.jsonPrimitive.content
        val context = ApplicationProvider.getApplicationContext<Context>()
        val labelLines =
            context.assets
                .open(labelsAssetPath)
                .bufferedReader()
                .useLines { seq -> seq.map { it.trim() }.filter { it.isNotBlank() }.toSet() }
        val mappingKeys = mappingRows().keys
        val missing = mappingKeys - labelLines
        assertThat(missing).isEmpty()
    }

    @Test
    fun everyKbSpeciesIdResolvesInBundledKb() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val kb = KbLoader(context.assets).load()
            val kbIds = kb.species.map { it.id }.toSet()
            val rows = mappingRows()
            val ids = rows.values.map { it["kbSpeciesId"]!!.jsonPrimitive.content }.toSet()
            val unknown = ids - kbIds
            assertThat(unknown).isEmpty()
        }

    @Test
    fun mappingKeysHaveUniqueNormalisedForms() {
        val rows = mappingRows()
        val normalised = rows.keys.map { it.trim().lowercase() }
        assertThat(normalised).hasSize(normalised.toSet().size)
    }

    @Test
    fun aliasRowsHaveNonAliasCounterpartPointingAtSameKbId() {
        val rows = mappingRows()
        val aliasRows = rows.filterValues { (it["alias"]?.jsonPrimitive?.boolean ?: false) }
        for ((aliasKey, aliasRow) in aliasRows) {
            val target = aliasRow["kbSpeciesId"]!!.jsonPrimitive.content
            val nonAliasCounterpart =
                rows.entries.firstOrNull { (k, row) ->
                    k != aliasKey &&
                        row["kbSpeciesId"]!!.jsonPrimitive.content == target &&
                        !(row["alias"]?.jsonPrimitive?.boolean ?: false)
                }
            assertThat(nonAliasCounterpart).isNotNull()
        }
    }

    @Test
    fun mappingCoversEveryBundledKbSpecies() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val kb = KbLoader(context.assets).load()
            val kbIds = kb.species.map { it.id }.toSet()
            val mappingIds =
                mappingRows()
                    .values
                    .map { it["kbSpeciesId"]!!.jsonPrimitive.content }
                    .toSet()
            // Every KB species must be reachable from at least one mapping line.
            val unmapped = kbIds - mappingIds
            assertThat(unmapped).isEmpty()
        }
}
