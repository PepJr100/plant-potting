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
            if (jpeg.isEmpty()) {
                throw IdentificationFailureException("Empty JPEG byte array")
            }
            val bitmap: Bitmap =
                BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
                    ?: throw IdentificationFailureException("Malformed JPEG (BitmapFactory returned null)")
            if (bitmap.width <= 0 || bitmap.height <= 0) {
                throw IdentificationFailureException("Decoded JPEG has zero dimensions")
            }
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

        private fun ensureMutableArgb(bitmap: Bitmap): Bitmap =
            if (bitmap.config == Bitmap.Config.ARGB_8888) {
                bitmap
            } else {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            }
    }
