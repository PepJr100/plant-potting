package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

/**
 * PLANTPOTTING-0007 §Phase 4 — JVM contract coverage for the **winning** bundle
 * (`house_plant_species_mobilenetv2`), alongside the AIY-bundle tests which stay green
 * (`ModelManifestTest` etc.) because AIY remains the default `ACTIVE_MODEL_ROOT`.
 *
 * Locks: manifest↔model.tflite sha256, label_count↔labels.csv, output shape, the float16 path's
 * `input_dtype=float32` + normalization stanza, license, and mapping integrity (10 entries → KB
 * ids; the 2 coarse maps present; `Calathea lancifolia` intentionally unmapped).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class CandidateBundleContractTest {
    private val dir = "ml/house_plant_species_mobilenetv2"
    private val json = Json { ignoreUnknownKeys = true }
    private val assets get() = ApplicationProvider.getApplicationContext<Context>().assets

    private fun asset(name: String): ByteArray = assets.open("$dir/$name").use { it.readBytes() }

    private fun manifestJson() = json.parseToJsonElement(String(asset("model_manifest.json"), Charsets.UTF_8)).jsonObject

    @Test
    fun manifestParsesViaProductionReader() {
        val m = ModelManifestReader(assets, dir).read()
        assertThat(m.placeholder).isFalse()
        assertThat(m.inputSize).isEqualTo(224)
        assertThat(m.labelCount).isEqualTo(47)
        assertThat(m.outputTensorShape).containsExactly(1, 47).inOrder()
        // float16 conversion → FLOAT32 input tensor → normalization mean=0,std=255 (x/255).
        assertThat(m.inputDtype.name).isEqualTo("FLOAT32")
        assertThat(m.normalization.std.toList()).containsExactly(255.0f, 255.0f, 255.0f)
    }

    @Test
    fun declaredSha256MatchesBundledModelBytes() {
        val expected =
            MessageDigest
                .getInstance("SHA-256")
                .digest(asset("model.tflite"))
                .joinToString("") { "%02x".format(it) }
        assertThat(manifestJson()["sha256"]!!.jsonPrimitive.content).isEqualTo(expected)
    }

    @Test
    fun declaredLabelCountMatchesLabelsCsv() {
        val lines =
            String(asset("labels.csv"), Charsets.UTF_8)
                .lineSequence()
                .filter { it.isNotBlank() }
                .count()
        assertThat(lines).isEqualTo(47)
        assertThat(manifestJson()["label_count"]!!.jsonPrimitive.content.toInt()).isEqualTo(47)
    }

    @Test
    fun licenseIsApache20AndPerSpeciesThresholdsHoldOnlyThePileaDirectCardBar() {
        assertThat(manifestJson()["license"]!!.jsonPrimitive.content).isEqualTo("Apache-2.0")
        // PLANTPOTTING-0013: empty until 0012; now carries exactly the Pilea direct-card bar
        // (pilea-peperomioides > 0.9661, see ml-mapping-notes.md + evidence/PLANTPOTTING-0013/). The
        // 0006 "never seed to bless a weak prediction" discipline still holds — this is an *elevated*
        // bar above the pothos→Pilea ceiling, not a relaxation, and is CI-bound > 0.9661 by
        // HousePlantClassMapValidationTest.pileaDirectCardThresholdMustExceedPothosCeiling.
        val perSpecies = manifestJson()["per_species_thresholds"]!!.jsonObject
        assertThat(perSpecies.keys).containsExactly("pilea-peperomioides")
        assertThat(perSpecies["pilea-peperomioides"]!!.jsonPrimitive.content.toFloat()).isGreaterThan(0.9661f)
    }

    @Test
    fun mappingResolvesTenKbSpeciesIncludingTheTwoCoarseMaps() {
        val map = ModelLabelMapReader(assets, dir).read()
        val resolved =
            listOf(
                "Monstera Deliciosa (Monstera deliciosa)" to "monstera-deliciosa",
                "Pothos (Ivy arum)" to "epipremnum-aureum",
                "Peace lily" to "spathiphyllum-wallisii",
                "Rubber Plant (Ficus elastica)" to "ficus-elastica",
                "Snake plant (Sanseviera)" to "dracaena-trifasciata",
                "ZZ Plant (Zamioculcas zamiifolia)" to "zamioculcas-zamiifolia",
                "Jade plant (Crassula ovata)" to "crassula-ovata",
                "African Violet (Saintpaulia ionantha)" to "saintpaulia-ionantha",
                "Orchid" to "phalaenopsis", // coarse
                "Calathea" to "goeppertia-orbifolia", // coarse/genus
            )
        for ((label, kbId) in resolved) {
            assertThat(map.lookup(label)?.kbSpeciesId).isEqualTo(kbId)
        }
        // The separate lancifolia class must NOT be mapped to orbifolia (different species).
        assertThat(map.lookup("Rattlesnake Plant (Calathea lancifolia)")).isNull()
    }
}
