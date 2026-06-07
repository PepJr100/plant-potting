package com.darkfactory.plantpotting.identify.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.darkfactory.plantpotting.identify.IdentificationFailureException
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Decodes a JPEG into a tensor sized to the model manifest's input contract.
 *
 * Branches on [ModelManifest.inputDtype]:
 *  - UINT8 (the shipped AIY V1/3 path): builds a `TensorImage(DataType.UINT8)`, resizes to
 *    `manifest.inputSize`, and skips `NormalizeOp` — the model's quantization params
 *    encode the centering, so feeding raw 0–255 bytes is the correct contract.
 *  - FLOAT32: builds a `TensorImage(DataType.FLOAT32)`, resizes, and applies
 *    `NormalizeOp(manifest.normalization.mean, manifest.normalization.std)`.
 *
 * Branches on [ModelManifest.preprocessMode] (PLANTPOTTING-0011 Phase 2):
 *  - [PreprocessMode.SQUASH] (default — the historical, byte-for-byte path): the full frame is
 *    stretched to `inputSize × inputSize` by `ResizeOp`.
 *  - [PreprocessMode.CENTER_CROP]: the largest centred square is cropped first, then resized —
 *    aspect-ratio preserving, the TF-Hub MobileNetV2 convention.
 *
 * [preprocessVariants] supports multi-crop test-time augmentation (TTA): when
 * [ModelManifest.ttaCropCount] > 1 it emits a fixed, deterministic ensemble (centre + four
 * corners) for the caller to average softmax over. At the default count of 1 it returns exactly
 * the single [preprocess] tensor, so TTA is a no-op until a manifest default is flipped.
 *
 * Throws [IdentificationFailureException] on a malformed JPEG so the camera flow can route
 * to a clean "retake" state rather than masquerading model breakage as a low-confidence
 * result (PLANTPOTTING-0003 §4.3).
 */
@Singleton
class ImagePreprocessor
    @Inject
    constructor(
        private val manifest: ModelManifest,
    ) {
        private val tensorDataType: DataType =
            when (manifest.inputDtype) {
                ModelDtype.UINT8 -> DataType.UINT8
                ModelDtype.FLOAT32 -> DataType.FLOAT32
            }

        // Resizes an (already square-fitted, for CENTER_CROP) bitmap to the model input and applies
        // FLOAT32 normalization. For SQUASH this single ResizeOp performs the historical squash.
        private val processor: ImageProcessor =
            ImageProcessor
                .Builder()
                .add(ResizeOp(manifest.inputSize, manifest.inputSize, ResizeOp.ResizeMethod.BILINEAR))
                .also { builder ->
                    if (manifest.inputDtype == ModelDtype.FLOAT32) {
                        builder.add(NormalizeOp(manifest.normalization.mean, manifest.normalization.std))
                    }
                }.build()

        fun preprocess(jpeg: ByteArray): PreprocessedImage {
            val bitmap = decode(jpeg)
            val fitted = fitToSquare(bitmap)
            return toTensor(fitted)
        }

        /**
         * Test-time augmentation ensemble. Returns one tensor per view:
         *  - `ttaCropCount <= 1` → exactly `[preprocess(jpeg)]` (byte-for-byte the single-crop path).
         *  - otherwise → centre square + four corner squares + the **full-frame** (no-crop) squash
         *    (6 views max), each resized to the model input. The full-frame view restores the plant's
         *    overall shape that the zoomed crops lose. Deterministic (fixed geometry, no RNG).
         */
        fun preprocessVariants(jpeg: ByteArray): List<PreprocessedImage> {
            val bitmap = decode(jpeg)
            if (manifest.ttaCropCount <= 1) {
                return listOf(toTensor(fitToSquare(bitmap)))
            }
            return cropVariants(bitmap).map { toTensor(it) }
        }

        private fun decode(jpeg: ByteArray): Bitmap {
            if (jpeg.isEmpty()) {
                throw IdentificationFailureException("Empty JPEG byte array")
            }
            val bitmap: Bitmap =
                BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
                    ?: throw IdentificationFailureException("Malformed JPEG (BitmapFactory returned null)")
            if (bitmap.width <= 0 || bitmap.height <= 0) {
                throw IdentificationFailureException("Decoded JPEG has zero dimensions")
            }
            return bitmap
        }

        private fun toTensor(bitmap: Bitmap): PreprocessedImage {
            val sized = ensureMutableArgb(bitmap)
            val tensorImage = TensorImage(tensorDataType)
            tensorImage.load(sized)
            val processed = processor.process(tensorImage)
            return PreprocessedImage(
                width = manifest.inputSize,
                height = manifest.inputSize,
                buffer = processed.buffer,
            )
        }

        // SQUASH → identity (the ResizeOp does the stretch). CENTER_CROP → largest centred square.
        private fun fitToSquare(bitmap: Bitmap): Bitmap =
            when (manifest.preprocessMode) {
                PreprocessMode.SQUASH -> bitmap
                PreprocessMode.CENTER_CROP -> centerSquare(bitmap)
            }

        private fun centerSquare(bitmap: Bitmap): Bitmap {
            val side = min(bitmap.width, bitmap.height)
            if (side == bitmap.width && side == bitmap.height) return bitmap
            val x = (bitmap.width - side) / 2
            val y = (bitmap.height - side) / 2
            return Bitmap.createBitmap(bitmap, x, y, side, side)
        }

        // Deterministic TTA ensemble, built so `take(6)` is byte-for-byte the shipped tta6 set and
        // higher counts only APPEND views (production stays unchanged at tta=6):
        //   views 1–6   centre + 4 corners (0.8×) + full frame  (the shipped set)
        //   views 7–10  2×2 uniform grid tiles                  (PLANTPOTTING-0012 sweep → ×10)
        //   views 11–19 3×3 uniform grid tiles                  (PLANTPOTTING-0012 sweep → ×19)
        // The grid tiles cover regions the centre/corner set under-samples WITHOUT re-scoring the
        // same zones (each tile is a distinct, non-overlapping cell of the frame, squashed to the
        // model input). Only the first `ttaCropCount` are materialised. Fixed geometry, no RNG.
        private fun cropVariants(bitmap: Bitmap): List<Bitmap> {
            val n = manifest.ttaCropCount
            val shortSide = min(bitmap.width, bitmap.height)
            val corner = (shortSide * CORNER_CROP_FRACTION).roundToInt().coerceAtLeast(1)
            val maxX = bitmap.width - corner
            val maxY = bitmap.height - corner
            val base =
                listOf(
                    centerSquare(bitmap),
                    Bitmap.createBitmap(bitmap, 0, 0, corner, corner), // top-left
                    Bitmap.createBitmap(bitmap, maxX, 0, corner, corner), // top-right
                    Bitmap.createBitmap(bitmap, 0, maxY, corner, corner), // bottom-left
                    Bitmap.createBitmap(bitmap, maxX, maxY, corner, corner), // bottom-right
                    bitmap, // full frame (no crop) — toTensor() squashes it to the model input
                )
            if (n <= base.size) return base.take(n)

            // --- experiment-only extension (PLANTPOTTING-0012 Phase 7); never reached at tta<=6 ---
            val views = base.toMutableList()
            views += gridTiles(bitmap, 2) // 7–10: 2×2 grid
            if (n > views.size) views += gridTiles(bitmap, 3) // 11–19: 3×3 grid
            return views.take(n)
        }

        // k×k uniform, non-overlapping tiles of the frame (row-major). Each tile is squashed to the
        // model input by toTensor(), like the full-frame view — new spatial coverage, no re-score.
        private fun gridTiles(
            bitmap: Bitmap,
            k: Int,
        ): List<Bitmap> {
            val tiles = ArrayList<Bitmap>(k * k)
            for (r in 0 until k) {
                for (c in 0 until k) {
                    val x0 = bitmap.width * c / k
                    val y0 = bitmap.height * r / k
                    val w = (bitmap.width * (c + 1) / k) - x0
                    val h = (bitmap.height * (r + 1) / k) - y0
                    tiles += Bitmap.createBitmap(bitmap, x0, y0, w.coerceAtLeast(1), h.coerceAtLeast(1))
                }
            }
            return tiles
        }

        private fun ensureMutableArgb(bitmap: Bitmap): Bitmap =
            if (bitmap.config == Bitmap.Config.ARGB_8888) {
                bitmap
            } else {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            }

        private companion object {
            const val CORNER_CROP_FRACTION = 0.8f
        }
    }
