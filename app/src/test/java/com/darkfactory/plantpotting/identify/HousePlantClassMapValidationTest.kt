package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.kb.KbLoader
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0009 — referential-integrity / coverage gate over the **house-plant**
 * `plant_class_map.json` (none existed before this sprint; mirrors the AIY-scoped
 * [ModelLabelMappingValidationTest] but pointed at `ml/house_plant_species_mobilenetv2`).
 *
 * Runtime lookup against this model is EXACT-MATCH on the verbatim label string, so the
 * dominant defect class is label truncation (dropping a parenthetical). The "every key is a
 * verbatim line in labels.csv" assertion is the CI defence against that.
 *
 * NOTE: this test deliberately does NOT import the AIY alias-counterpart rule
 * (`aliasRowsHaveNonAliasCounterpartPointingAtSameKbId`). The house-plant map intentionally
 * carries coarse `alias:true` rows with no non-alias counterpart (e.g. the pre-existing
 * `Orchid` and `Calathea` rows, and the new genus-level coarse rows).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class HousePlantClassMapValidationTest {
    private val dir = "ml/house_plant_species_mobilenetv2"
    private val json = Json { ignoreUnknownKeys = true }

    /** The 16 PLANTPOTTING-0009 delta labels → expected KB id (verbatim from the plan §3). */
    private val newLabelToKbId =
        mapOf(
            "Chinese evergreen (Aglaonema)" to "aglaonema",
            "Elephant Ear (Alocasia spp.)" to "alocasia",
            "Anthurium (Anthurium andraeanum)" to "anthurium-andraeanum",
            "Dumb Cane (Dieffenbachia spp.)" to "dieffenbachia",
            "Aloe Vera" to "aloe-vera",
            "Kalanchoe" to "kalanchoe",
            "Prayer Plant (Maranta leuconeura)" to "maranta-leuconeura",
            "Boston Fern (Nephrolepis exaltata)" to "nephrolepis-exaltata",
            "Money Tree (Pachira aquatica)" to "pachira-aquatica",
            "Areca Palm (Dypsis lutescens)" to "dypsis-lutescens",
            "Dracaena" to "dracaena",
            "Tradescantia" to "tradescantia",
            "English Ivy (Hedera helix)" to "hedera-helix",
            "Schefflera" to "schefflera",
            "Poinsettia (Euphorbia pulcherrima)" to "euphorbia-pulcherrima",
            "Venus Flytrap" to "dionaea-muscipula",
        )

    /** The 12 PLANTPOTTING-0010 delta labels → expected KB id (verbatim from the plan §Phase 2). */
    private val newLabel2010ToKbId =
        mapOf(
            "Parlor Palm (Chamaedorea elegans)" to "chamaedorea-elegans",
            "Bird of Paradise (Strelitzia reginae)" to "strelitzia-reginae",
            "Cast Iron Plant (Aspidistra elatior)" to "aspidistra-elatior",
            "Birds Nest Fern (Asplenium nidus)" to "asplenium-nidus",
            "Asparagus Fern (Asparagus setaceus)" to "asparagus-setaceus",
            "Begonia (Begonia spp.)" to "begonia",
            "Polka Dot Plant (Hypoestes phyllostachya)" to "hypoestes-phyllostachya",
            "Ponytail Palm (Beaucarnea recurvata)" to "beaucarnea-recurvata",
            "Sago Palm (Cycas revoluta)" to "cycas-revoluta",
            "Yucca" to "yucca",
            "Ctenanthe" to "ctenanthe",
            "Christmas Cactus (Schlumbergera bridgesii)" to "schlumbergera-bridgesii",
        )

    /** The 10 pre-existing mapped rows → KB id (positive regression guard). */
    private val existingLabelToKbId =
        mapOf(
            "Monstera Deliciosa (Monstera deliciosa)" to "monstera-deliciosa",
            "Pothos (Ivy arum)" to "epipremnum-aureum",
            "Peace lily" to "spathiphyllum-wallisii",
            "Rubber Plant (Ficus elastica)" to "ficus-elastica",
            "Snake plant (Sanseviera)" to "dracaena-trifasciata",
            "ZZ Plant (Zamioculcas zamiifolia)" to "zamioculcas-zamiifolia",
            "Jade plant (Crassula ovata)" to "crassula-ovata",
            "African Violet (Saintpaulia ionantha)" to "saintpaulia-ionantha",
            "Orchid" to "phalaenopsis",
            "Calathea" to "goeppertia-orbifolia",
        )

    private fun readAsset(name: String): ByteArray {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.assets.open("$dir/$name").use { it.readBytes() }
    }

    private fun mappingObject(): JsonObject {
        val raw = String(readAsset("plant_class_map.json"), Charsets.UTF_8)
        return json.parseToJsonElement(raw).jsonObject
    }

    private fun mappingRows(): Map<String, JsonObject> =
        mappingObject()["mapping"]!!.jsonObject.entries.associate { (k, v) -> k to v.jsonObject }

    private fun labelLines(): Set<String> {
        val labelsPath = mappingObject()["modelLabelsAsset"]!!.jsonPrimitive.content
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.assets
            .open(labelsPath)
            .bufferedReader()
            .useLines { seq -> seq.map { it.trim() }.filter { it.isNotBlank() }.toSet() }
    }

    @Test
    fun mappingIsValidJsonAndDeclaresHousePlantLabelsAsset() {
        val obj = mappingObject()
        assertThat(obj.keys).containsAtLeast("version", "modelLabelsAsset", "mapping")
        assertThat(obj["modelLabelsAsset"]!!.jsonPrimitive.content)
            .isEqualTo("ml/house_plant_species_mobilenetv2/labels.csv")
    }

    @Test
    fun everyKbSpeciesIdResolvesInBundledKb() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val kb = KbLoader(context.assets).load()
            val kbIds = kb.species.map { it.id }.toSet()
            val ids = mappingRows().values.map { it["kbSpeciesId"]!!.jsonPrimitive.content }.toSet()
            assertThat(ids - kbIds).isEmpty()
        }

    @Test
    fun everyMappingKeyIsVerbatimLabelLine() {
        // The dominant defect: label truncation silently fails to resolve at runtime.
        val labels = labelLines()
        val missing = mappingRows().keys - labels
        assertWithMessage("mapping keys absent from labels.csv").that(missing).isEmpty()
    }

    @Test
    fun mapsExactlyThirtyNineClasses() {
        // 10 (0007) + 16 (0009) + 12 (0010) + 1 (0012: Pilea) — guards both accidental extras and silent drops.
        assertThat(mappingRows()).hasSize(39)
    }

    @Test
    fun eachNewLabelResolvesToExpectedKbId() {
        val rows = mappingRows()
        for ((label, expectedId) in newLabelToKbId + newLabel2010ToKbId) {
            val row = rows[label]
            assertWithMessage("new label '$label' present").that(row).isNotNull()
            assertWithMessage("new label '$label' kbSpeciesId")
                .that(row!!["kbSpeciesId"]!!.jsonPrimitive.content)
                .isEqualTo(expectedId)
        }
    }

    @Test
    fun existingTenRowsStillPointAtSameKbIds() {
        val rows = mappingRows()
        for ((label, expectedId) in existingLabelToKbId) {
            val row = rows[label]
            assertWithMessage("existing label '$label' present").that(row).isNotNull()
            assertWithMessage("existing label '$label' kbSpeciesId unchanged")
                .that(row!!["kbSpeciesId"]!!.jsonPrimitive.content)
                .isEqualTo(expectedId)
        }
    }

    @Test
    fun pileaMapsToPileaPeperomioides() {
        // PLANTPOTTING-0012: the 0009 deferral is lifted — Pilea is now mapped (in lockstep with the gate).
        val row = mappingRows()["Chinese Money Plant (Pilea peperomioides)"]
        assertWithMessage("Pilea mapping row present").that(row).isNotNull()
        assertThat(row!!["kbSpeciesId"]!!.jsonPrimitive.content).isEqualTo("pilea-peperomioides")
    }

    @Test
    fun pileaMappingRequiresBoundaryGate() {
        // PLANTPOTTING-0012 bind-mapping-to-gate guard: IF Pilea is mapped, the production manifest
        // MUST carry a pothos↔Pilea boundary rule that routes a top-1 = pilea-peperomioides result to
        // the picker. A tree that maps Pilea without the gate (the confidently-wrong window the sprint
        // exists to close) turns this red.
        val pileaMapped =
            mappingRows()["Chinese Money Plant (Pilea peperomioides)"]
                ?.get("kbSpeciesId")
                ?.jsonPrimitive
                ?.content == "pilea-peperomioides"
        if (!pileaMapped) return // vacuously satisfied if Pilea is ever un-mapped again

        val manifestRaw = String(readAsset("model_manifest.json"), Charsets.UTF_8)
        val pairs: JsonArray =
            json.parseToJsonElement(manifestRaw).jsonObject["boundary_pairs"]?.jsonArray ?: JsonArray(emptyList())
        val triggers =
            pairs.map { it.jsonObject["top1_kb_species_id"]!!.jsonPrimitive.content }
        assertWithMessage("mapping Pilea requires a boundary_pairs rule with top1=pilea-peperomioides")
            .that(triggers)
            .contains("pilea-peperomioides")
    }

    @Test
    fun pileaDirectCardThresholdMustExceedPothosCeiling() {
        // PLANTPOTTING-0013 config-level CI bind (the tripwire that makes "pothos→Pilea can never reach
        // a direct Pilea card" enforceable below the on-device eval). IF the manifest carries an
        // elevated direct-card threshold for Pilea, it MUST be strictly above the documented pothos→Pilea
        // ceiling of 0.9661 — so a pothos misread as Pilea @ 0.9661 can never clear it. Absent (the
        // strict-picker default) is also valid. See docs/sprints/evidence/PLANTPOTTING-0013/.
        val manifestRaw = String(readAsset("model_manifest.json"), Charsets.UTF_8)
        val perSpecies =
            json.parseToJsonElement(manifestRaw).jsonObject["per_species_thresholds"]?.jsonObject
        val pileaThreshold = perSpecies?.get("pilea-peperomioides")?.jsonPrimitive?.float
        if (pileaThreshold != null) {
            assertWithMessage(
                "per_species_thresholds[pilea-peperomioides] must be > 0.9661 (the documented pothos→Pilea ceiling)",
            ).that(pileaThreshold).isGreaterThan(0.9661f)
        }
    }
}
