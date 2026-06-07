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
    // PLANTPOTTING-0005 §5.2 — optional per-species override of `Thresholds.highConfidencePlain`.
    // Keyed by KB `speciesId`. Empty by default; seed only with probe evidence (§4.6).
    val perSpeciesThresholds: Map<String, Float> = emptyMap(),
    // PLANTPOTTING-0011 Phase 2 — preprocessing levers, default-disabled so the shipping
    // pipeline is byte-for-byte unchanged until the eval harness justifies flipping a default.
    //   preprocessMode = SQUASH   → the current non-aspect-preserving ResizeOp (the A/B control).
    //   ttaCropCount   = 1        → single crop = current behaviour; >1 enables multi-crop TTA.
    val preprocessMode: PreprocessMode = PreprocessMode.SQUASH,
    val ttaCropCount: Int = 1,
    // PLANTPOTTING-0012 Phase 4 — targeted disambiguation gate, default-empty so any other model
    // is unaffected. When a result's raw top-1 label resolves to a pair's `top1KbSpeciesId`, the
    // verdict is forced to low-confidence (routed to the picker) and every `surfaceKbSpeciesIds`
    // member is guaranteed visible as a candidate. Composes WITH the abstain margin, never replaces
    // it. See docs/sprints/evidence/PLANTPOTTING-0012/boundary-gating-decision.md (Candidate B).
    val boundaryPairs: List<BoundaryPair> = emptyList(),
) {
    /**
     * A pothos↔Pilea-style boundary rule. If the raw top-1 label resolves to [top1KbSpeciesId], the
     * mapper forces the low-confidence route and surfaces all [surfaceKbSpeciesIds] as candidates so
     * a confident single-class misread (e.g. pothos→Pilea @ 0.9661, no second-place mass) can never
     * become a confidently-wrong direct care card.
     */
    data class BoundaryPair(
        val top1KbSpeciesId: String,
        val surfaceKbSpeciesIds: List<String>,
    )

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
        // PLANTPOTTING-0011 Phase 3 — margin-based abstention *above* the plain gate.
        // If a verdict would be high-confidence but `(bestProb - secondProb)` is below this
        // margin, downgrade it to low-confidence (route to the picker). Default `0f` = disabled
        // (no-op), so AIY and the existing baseline stay byte-for-byte unchanged.
        val highConfidenceAbstainMargin: Float = 0f,
    )
}

/**
 * How [ImagePreprocessor] fits the decoded bitmap to the model's square input.
 *
 *  - [SQUASH]: the historical non-aspect-preserving `ResizeOp` — stretches the full frame to
 *    `inputSize × inputSize`. The PLANTPOTTING-0011 A/B control (current shipping behaviour).
 *  - [CENTER_CROP]: crop the largest centred square first, then resize — preserves aspect ratio
 *    at the cost of edge context. The TF-Hub MobileNetV2 feature-vector convention.
 */
enum class PreprocessMode {
    SQUASH,
    CENTER_CROP,
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

            val perSpeciesThresholds: Map<String, Float> =
                obj["per_species_thresholds"]?.jsonObject?.mapValues { (_, v) ->
                    v.jsonPrimitive.float
                } ?: emptyMap()

            // PLANTPOTTING-0011 — optional preprocessing levers; default to current behaviour
            // when absent so an un-bumped manifest keeps the byte-for-byte shipping pipeline.
            val preprocessMode =
                when (val raw = obj["preprocess_mode"]?.jsonPrimitive?.content) {
                    null, "squash" -> PreprocessMode.SQUASH
                    "center_crop" -> PreprocessMode.CENTER_CROP
                    else ->
                        error(
                            "model_manifest.json: invalid preprocess_mode '$raw' " +
                                "(expected 'squash' or 'center_crop')",
                        )
                }
            val ttaCropCount = obj["tta"]?.jsonPrimitive?.int ?: 1

            // PLANTPOTTING-0012 — optional boundary-pair gate; absent → empty (no-op for any model
            // without it). Each entry requires a `route` of "low_confidence" (the only behaviour);
            // an unknown route is a hard error so a typo can't silently disable the gate.
            val boundaryPairs: List<ModelManifest.BoundaryPair> =
                obj["boundary_pairs"]?.jsonArray?.map { el ->
                    val o = el.jsonObject
                    val route = o["route"]?.jsonPrimitive?.content ?: "low_confidence"
                    if (route != "low_confidence") {
                        error("model_manifest.json: boundary_pairs route '$route' (expected 'low_confidence')")
                    }
                    ModelManifest.BoundaryPair(
                        top1KbSpeciesId = o["top1_kb_species_id"]!!.jsonPrimitive.content,
                        surfaceKbSpeciesIds =
                            (o["surface_kb_species_ids"] as JsonArray).map { it.jsonPrimitive.content },
                    )
                } ?: emptyList()

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
                        highConfidenceAbstainMargin =
                            thresholds["high_confidence_abstain_margin"]?.jsonPrimitive?.float ?: 0f,
                    ),
                perSpeciesThresholds = perSpeciesThresholds,
                preprocessMode = preprocessMode,
                ttaCropCount = ttaCropCount,
                boundaryPairs = boundaryPairs,
            )
        }
    }
}
