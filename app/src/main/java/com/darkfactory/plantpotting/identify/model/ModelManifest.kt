package com.darkfactory.plantpotting.identify.model

import android.content.res.AssetManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * In-memory projection of `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`.
 * Holds the input contract (size / color order / input dtype / normalization), the
 * output tensor shape, the label count, and the §4.3 confidence-policy numbers.
 *
 * `normalization` defaults to a no-op (mean = 0, std = 1) for UINT8 models — the
 * preprocessor branches on `inputDtype` and only applies `NormalizeOp` on the FLOAT32
 * path. PLANTPOTTING-0004 §1.2 / Decision §4.1.
 */
data class ModelManifest(
    val variant: String,
    val sha256: String,
    val placeholder: Boolean,
    val inputSize: Int,
    val inputDtype: ModelDtype,
    val colorOrder: String,
    val normalization: Normalization,
    val outputTensorShape: List<Int>,
    val labelCount: Int,
    val labelsAsset: String,
    val mappingAsset: String,
    val thresholds: Thresholds,
) {
    data class Normalization(
        val mean: FloatArray,
        val std: FloatArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Normalization) return false
            return mean.contentEquals(other.mean) && std.contentEquals(other.std)
        }

        override fun hashCode(): Int = 31 * mean.contentHashCode() + std.contentHashCode()
    }

    data class Thresholds(
        val highConfidencePlain: Float,
        val highConfidenceMarginMin: Float,
        val highConfidenceMarginDelta: Float,
        val topKCandidates: Int,
    )
}

/**
 * The input-tensor data type the manifest declares for the shipped `.tflite`. The
 * preprocessor branches on this; the interpreter facade asserts the runtime tensor
 * type agrees at load time. PLANTPOTTING-0004 Decision §4.1.
 */
enum class ModelDtype {
    UINT8,
    FLOAT32,
}

/**
 * Reads the on-device model manifest from the AssetManager. Constructor parameter
 * `dir` defaults to `ml/aiy_plants_v1` (the only model the app ships today); a future
 * sprint that introduces variant-switching could thread a different directory through.
 */
class ModelManifestReader(
    private val assets: AssetManager,
    private val dir: String = "ml/aiy_plants_v1",
) {
    fun read(): ModelManifest {
        val raw =
            assets
                .open("$dir/model_manifest.json")
                .use { it.readBytes() }
                .let { String(it, Charsets.UTF_8) }
        return parse(raw)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parses a raw `model_manifest.json` string into a [ModelManifest]. Exposed for
         * the JVM-side rejection tests (PLANTPOTTING-0004 §0.7) that need to feed
         * deliberately-malformed JSON through the same path the production reader uses.
         */
        fun parse(rawJson: String): ModelManifest {
            val obj = json.parseToJsonElement(rawJson).jsonObject

            val dtypePrim = obj["input_dtype"]?.jsonPrimitive
            val dtypeRaw = dtypePrim?.content
            val inputDtype =
                when (dtypeRaw) {
                    "uint8" -> ModelDtype.UINT8
                    "float32" -> ModelDtype.FLOAT32
                    null -> error("model_manifest.json: missing required field input_dtype")
                    else ->
                        error(
                            "model_manifest.json: invalid input_dtype '$dtypeRaw' (expected 'uint8' or 'float32')",
                        )
                }

            val normObj = obj["normalization"]?.jsonObject
            val normalization =
                if (normObj != null) {
                    val mean = (normObj["mean"] as JsonArray).map { it.jsonPrimitive.float }.toFloatArray()
                    val std = (normObj["std"] as JsonArray).map { it.jsonPrimitive.float }.toFloatArray()
                    ModelManifest.Normalization(mean = mean, std = std)
                } else {
                    // UINT8 path: normalization is encoded in tensor quantization params,
                    // not externally — feed an identity stanza so the data class stays
                    // simple. The preprocessor never reads this on the UINT8 branch.
                    ModelManifest.Normalization(
                        mean = floatArrayOf(0f, 0f, 0f),
                        std = floatArrayOf(1f, 1f, 1f),
                    )
                }

            val shape = obj["output_tensor_shape"]!!.jsonArray.map { it.jsonPrimitive.int }
            val thresholds = obj["thresholds"]!!.jsonObject

            return ModelManifest(
                variant = obj["variant"]!!.jsonPrimitive.content,
                sha256 = obj["sha256"]!!.jsonPrimitive.content,
                placeholder = obj["placeholder"]?.jsonPrimitive?.boolean ?: false,
                inputSize = obj["input_size"]!!.jsonPrimitive.int,
                inputDtype = inputDtype,
                colorOrder = obj["color_order"]!!.jsonPrimitive.content,
                normalization = normalization,
                outputTensorShape = shape,
                labelCount = obj["label_count"]!!.jsonPrimitive.int,
                labelsAsset = obj["labels_asset"]!!.jsonPrimitive.content,
                mappingAsset = obj["mapping_asset"]!!.jsonPrimitive.content,
                thresholds =
                    ModelManifest.Thresholds(
                        highConfidencePlain = thresholds["high_confidence_plain"]!!.jsonPrimitive.float,
                        highConfidenceMarginMin = thresholds["high_confidence_margin_min"]!!.jsonPrimitive.float,
                        highConfidenceMarginDelta = thresholds["high_confidence_margin_delta"]!!.jsonPrimitive.float,
                        topKCandidates = thresholds["top_k_candidates"]!!.jsonPrimitive.int,
                    ),
            )
        }
    }
}
