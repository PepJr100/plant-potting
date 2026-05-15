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
 * Holds the input contract (size / color order / normalization), the output tensor
 * shape, the label count, and the §4.3 confidence-policy numbers.
 *
 * The fields the model_manifest_test asserts against (sha256, label_count, thresholds)
 * are surfaced here verbatim so the production code path also reads them, rather than
 * duplicating the §4.3 numbers in Kotlin.
 */
data class ModelManifest(
    val variant: String,
    val sha256: String,
    val placeholder: Boolean,
    val inputSize: Int,
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
 * Reads the on-device model manifest from the AssetManager. Constructor parameter
 * `dir` defaults to `ml/aiy_plants_v1` (the only model the app ships today); a future
 * sprint that introduces variant-switching could thread a different directory through.
 */
class ModelManifestReader(
    private val assets: AssetManager,
    private val dir: String = "ml/aiy_plants_v1",
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun read(): ModelManifest {
        val raw =
            assets
                .open("$dir/model_manifest.json")
                .use { it.readBytes() }
                .let { String(it, Charsets.UTF_8) }
        val obj = json.parseToJsonElement(raw).jsonObject

        val norm = obj["normalization"]!!.jsonObject
        val mean = (norm["mean"] as JsonArray).map { it.jsonPrimitive.float }.toFloatArray()
        val std = (norm["std"] as JsonArray).map { it.jsonPrimitive.float }.toFloatArray()

        val shape = obj["output_tensor_shape"]!!.jsonArray.map { it.jsonPrimitive.int }

        val thresholds = obj["thresholds"]!!.jsonObject
        return ModelManifest(
            variant = obj["variant"]!!.jsonPrimitive.content,
            sha256 = obj["sha256"]!!.jsonPrimitive.content,
            placeholder = obj["placeholder"]?.jsonPrimitive?.boolean ?: false,
            inputSize = obj["input_size"]!!.jsonPrimitive.int,
            colorOrder = obj["color_order"]!!.jsonPrimitive.content,
            normalization = ModelManifest.Normalization(mean = mean, std = std),
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
