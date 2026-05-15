package com.darkfactory.plantpotting.identify.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.darkfactory.plantpotting.identify.IdentificationFailureException
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

/**
 * Decodes a JPEG into a normalised RGB tensor sized to the model manifest's input contract.
 *
 * Reads `inputSize` / `colorOrder` / `normalization` from the manifest at construction time,
 * so swapping in a different model (e.g. INT8 variant per §3.2 stretch) requires only an
 * asset swap, no code change.
 *
 * Throws [IdentificationFailureException] on a malformed JPEG so the camera flow can route
 * to a clean "retake" state rather than masquerading model breakage as a low-confidence
 * result (PLANTPOTTING-0003 §4.3).
 */
class ImagePreprocessor(
    private val manifest: ModelManifest,
) {
    private val processor: ImageProcessor =
        ImageProcessor
            .Builder()
            .add(ResizeOp(manifest.inputSize, manifest.inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(manifest.normalization.mean, manifest.normalization.std))
            .build()

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
        val tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(sized)
        val processed = processor.process(tensorImage)
        val floats = processed.tensorBuffer.floatArray
        return PreprocessedImage(
            width = manifest.inputSize,
            height = manifest.inputSize,
            normalisedRgb = floats,
        )
    }

    private fun ensureMutableArgb(bitmap: Bitmap): Bitmap =
        if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
}
