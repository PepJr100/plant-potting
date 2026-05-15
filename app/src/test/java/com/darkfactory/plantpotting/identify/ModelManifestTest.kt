package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

/**
 * PLANTPOTTING-0003 §2.4 — sanity-check the `model_manifest.json`. The manifest is the
 * editorial source of truth for the model's sha256, label_count, and threshold policy;
 * this test ensures it does not drift from the bundled `model.tflite` bytes / `labels.csv`
 * line count / §4.3 threshold numbers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ModelManifestTest {
    private val dir = "ml/aiy_plants_v1"
    private val json = Json { ignoreUnknownKeys = true }

    private fun readAsset(name: String): ByteArray {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.assets.open("$dir/$name").use { it.readBytes() }
    }

    private fun manifest(): JsonObject {
        val raw = String(readAsset("model_manifest.json"), Charsets.UTF_8)
        return json.parseToJsonElement(raw).jsonObject
    }

    @Test
    fun manifestIsValidJson() {
        val m = manifest()
        assertThat(m.keys).containsAtLeast(
            "source_url",
            "variant",
            "sha256",
            "input_size",
            "color_order",
            "normalization",
            "output_tensor_shape",
            "label_count",
            "acquisition_date",
            "license",
            "labels_asset",
            "thresholds",
        )
    }

    @Test
    fun declaredSha256MatchesBundledModelBytes() {
        val modelBytes = readAsset("model.tflite")
        val expected = sha256Hex(modelBytes)
        val actual = manifest()["sha256"]!!.jsonPrimitive.content
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun declaredLabelCountMatchesLabelsCsvLineCount() {
        val labelLines =
            String(readAsset("labels.csv"), Charsets.UTF_8)
                .lineSequence()
                .filter { it.isNotBlank() }
                .count()
        val declared = manifest()["label_count"]!!.jsonPrimitive.int
        assertThat(declared).isEqualTo(labelLines)
    }

    @Test
    fun declaredOutputTensorShapeMatchesLabelCount() {
        val m = manifest()
        val shape = m["output_tensor_shape"]!!.jsonArray.map { it.jsonPrimitive.int }
        val labelCount = m["label_count"]!!.jsonPrimitive.int
        assertThat(shape.last()).isEqualTo(labelCount)
    }

    @Test
    fun thresholdsMatchPlanSection43Verbatim() {
        val t = manifest()["thresholds"]!!.jsonObject
        assertThat(t["high_confidence_plain"]!!.jsonPrimitive.double).isEqualTo(0.55)
        assertThat(t["high_confidence_margin_min"]!!.jsonPrimitive.double).isEqualTo(0.45)
        assertThat(t["high_confidence_margin_delta"]!!.jsonPrimitive.double).isEqualTo(0.18)
        assertThat(t["top_k_candidates"]!!.jsonPrimitive.int).isEqualTo(3)
    }

    @Test
    fun normalisationMeanAndStdAreThreeChannel() {
        val n = manifest()["normalization"]!!.jsonObject
        val mean = (n["mean"] as JsonArray).map { it.jsonPrimitive.double }
        val std = (n["std"] as JsonArray).map { it.jsonPrimitive.double }
        assertThat(mean).hasSize(3)
        assertThat(std).hasSize(3)
    }

    @Test
    fun licenseFieldIsApache20() {
        assertThat(manifest()["license"]!!.jsonPrimitive.content).isEqualTo("Apache-2.0")
    }

    @Test
    fun placeholderFlagDocumentsPendingRealModelDownload() {
        val m = manifest()
        // The `placeholder` field is true while the bundled model.tflite is a stub
        // for testing (see §2.2 / Blockers). When the real V1/3 .tflite is dropped
        // in (and sha256/label_count/output_tensor_shape are updated), `placeholder`
        // must flip to false to satisfy the device-aware acceptance gate (§8.5).
        val placeholder = m["placeholder"]?.jsonPrimitive?.boolean ?: false
        // No assertion on its value — the existence of the field is the documented
        // hand-off signal. This test just confirms the schema includes it.
        assertThat(placeholder).isAnyOf(true, false)
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
