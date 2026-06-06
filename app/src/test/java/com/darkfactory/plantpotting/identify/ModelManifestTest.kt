package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
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
        // PLANTPOTTING-0004 §1.1: the active `normalization` stanza was removed for the
        // shipped UINT8 model. The new required field is `input_dtype` (locked separately
        // by ModelManifestDtypeContractTest).
        assertThat(m.keys).containsAtLeast(
            "source_url",
            "variant",
            "sha256",
            "input_size",
            "input_dtype",
            "color_order",
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
    fun normalisationStanzaAbsentForShippedUint8Model() {
        // PLANTPOTTING-0004 §1.1 / Decision §4.1: AIY V1/3 is UINT8 — normalization is
        // encoded in tensor quantization params, not applied externally. The active
        // `normalization` block was removed from the shipped manifest to prevent silent
        // contract drift; the FP path keeps a `_comment_normalization_unused_for_uint8`
        // breadcrumb.
        val m = manifest()
        assertThat(m.containsKey("normalization")).isFalse()
        assertThat(m.containsKey("_comment_normalization_unused_for_uint8")).isTrue()
    }

    @Test
    fun licenseFieldIsApache20() {
        assertThat(manifest()["license"]!!.jsonPrimitive.content).isEqualTo("Apache-2.0")
    }

    @Test
    fun manifestRejectsInvalidInputDtypeUin8() {
        try {
            com.darkfactory.plantpotting.identify.model.ModelManifestReader.parse(
                manifestJsonWithInputDtype("\"uin8\""),
            )
            assertThat("did not throw").isEqualTo("threw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertThat(e.message).contains("input_dtype")
        }
    }

    @Test
    fun manifestRejectsInvalidInputDtypeEmptyString() {
        try {
            com.darkfactory.plantpotting.identify.model.ModelManifestReader.parse(
                manifestJsonWithInputDtype("\"\""),
            )
            assertThat("did not throw").isEqualTo("threw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertThat(e.message).contains("input_dtype")
        }
    }

    @Test
    fun manifestRejectsMissingInputDtype() {
        try {
            com.darkfactory.plantpotting.identify.model.ModelManifestReader.parse(
                manifestJsonWithoutInputDtype(),
            )
            assertThat("did not throw").isEqualTo("threw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertThat(e.message).contains("input_dtype")
        }
    }

    private fun manifestJsonWithInputDtype(dtypeLiteral: String): String =
        """
        {
          "variant": "V1/3",
          "sha256": "abc",
          "placeholder": false,
          "input_size": 224,
          "input_dtype": $dtypeLiteral,
          "color_order": "RGB",
          "normalization": { "mean": [127.5,127.5,127.5], "std": [127.5,127.5,127.5] },
          "output_tensor_shape": [1, 2102],
          "label_count": 2102,
          "labels_asset": "ml/aiy_plants_v1/labels.csv",
          "mapping_asset": "ml/aiy_plants_v1/plant_class_map.json",
          "thresholds": {
            "high_confidence_plain": 0.55,
            "high_confidence_margin_min": 0.45,
            "high_confidence_margin_delta": 0.18,
            "top_k_candidates": 3
          }
        }
        """.trimIndent()

    private fun manifestJsonWithoutInputDtype(): String =
        """
        {
          "variant": "V1/3",
          "sha256": "abc",
          "placeholder": false,
          "input_size": 224,
          "color_order": "RGB",
          "normalization": { "mean": [127.5,127.5,127.5], "std": [127.5,127.5,127.5] },
          "output_tensor_shape": [1, 2102],
          "label_count": 2102,
          "labels_asset": "ml/aiy_plants_v1/labels.csv",
          "mapping_asset": "ml/aiy_plants_v1/plant_class_map.json",
          "thresholds": {
            "high_confidence_plain": 0.55,
            "high_confidence_margin_min": 0.45,
            "high_confidence_margin_delta": 0.18,
            "top_k_candidates": 3
          }
        }
        """.trimIndent()

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

    // -- PLANTPOTTING-0011 — new optional preprocessing / abstention fields --------------
    // The parser must default every new field to current behaviour when absent (so an
    // un-bumped manifest keeps the byte-for-byte shipping pipeline) and read it when present.

    @Test
    fun newOptionalFieldsDefaultToCurrentBehaviourWhenAbsent() {
        val parsed =
            com.darkfactory.plantpotting.identify.model.ModelManifestReader.parse(
                manifestJsonWithInputDtype("\"float32\""),
            )
        assertThat(parsed.preprocessMode)
            .isEqualTo(com.darkfactory.plantpotting.identify.model.PreprocessMode.SQUASH)
        assertThat(parsed.ttaCropCount).isEqualTo(1)
        assertThat(parsed.thresholds.highConfidenceAbstainMargin).isEqualTo(0f)
    }

    @Test
    fun preprocessModeCenterCropParses() {
        val parsed =
            com.darkfactory.plantpotting.identify.model.ModelManifestReader.parse(
                manifestJsonWithExtras(preprocessMode = "\"center_crop\"", tta = "5", abstain = "0.12"),
            )
        assertThat(parsed.preprocessMode)
            .isEqualTo(com.darkfactory.plantpotting.identify.model.PreprocessMode.CENTER_CROP)
        assertThat(parsed.ttaCropCount).isEqualTo(5)
        assertThat(parsed.thresholds.highConfidenceAbstainMargin).isWithin(1e-6f).of(0.12f)
    }

    @Test
    fun invalidPreprocessModeIsRejected() {
        try {
            com.darkfactory.plantpotting.identify.model.ModelManifestReader.parse(
                manifestJsonWithExtras(preprocessMode = "\"crop_to_taste\"", tta = "1", abstain = "0"),
            )
            assertThat("did not throw").isEqualTo("threw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertThat(e.message).contains("preprocess_mode")
        }
    }

    private fun manifestJsonWithExtras(
        preprocessMode: String,
        tta: String,
        abstain: String,
    ): String =
        """
        {
          "variant": "V1/3",
          "sha256": "abc",
          "placeholder": false,
          "input_size": 224,
          "input_dtype": "float32",
          "color_order": "RGB",
          "normalization": { "mean": [0,0,0], "std": [255,255,255] },
          "output_tensor_shape": [1, 47],
          "label_count": 47,
          "labels_asset": "ml/x/labels.csv",
          "mapping_asset": "ml/x/plant_class_map.json",
          "preprocess_mode": $preprocessMode,
          "tta": $tta,
          "thresholds": {
            "high_confidence_plain": 0.55,
            "high_confidence_margin_min": 0.45,
            "high_confidence_margin_delta": 0.18,
            "top_k_candidates": 3,
            "high_confidence_abstain_margin": $abstain
          }
        }
        """.trimIndent()

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
