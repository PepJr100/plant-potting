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

        // Centre full square + four corner squares (0.8× the short side) + the full frame (no crop),
        // deterministic geometry. The full-frame view is last so `take(5)` keeps the crop-only set.
        private fun cropVariants(bitmap: Bitmap): List<Bitmap> {
            val shortSide = min(bitmap.width, bitmap.height)
            val corner = (shortSide * CORNER_CROP_FRACTION).roundToInt().coerceAtLeast(1)
            val maxX = bitmap.width - corner
            val maxY = bitmap.height - corner
            return listOf(
                centerSquare(bitmap),
                Bitmap.createBitmap(bitmap, 0, 0, corner, corner), // top-left
                Bitmap.createBitmap(bitmap, maxX, 0, corner, corner), // top-right
                Bitmap.createBitmap(bitmap, 0, maxY, corner, corner), // bottom-left
                Bitmap.createBitmap(bitmap, maxX, maxY, corner, corner), // bottom-right
                bitmap, // full frame (no crop) — toTensor() squashes it to the model input
            ).take(manifest.ttaCropCount)
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
