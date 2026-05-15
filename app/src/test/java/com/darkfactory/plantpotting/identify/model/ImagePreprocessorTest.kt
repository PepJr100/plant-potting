package com.darkfactory.plantpotting.identify.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

/**
 * PLANTPOTTING-0003 §3.2 — verifies `ImagePreprocessor` honors the manifest input contract.
 *
 * Generates deterministic JPEGs in-memory (white 1×1, 100×100 gradient, 224×224 grey) and
 * asserts:
 *  - the produced tensor matches the manifest input size
 *  - normalization yields ≈ 1.0 for pure white and ≈ 0.0 for mean-grey (manifest mean = 127.5)
 *  - malformed JPEG bytes throw `IdentificationFailureException`
 *
 * RED before §3.3 lands the `ImagePreprocessor` class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ImagePreprocessorTest {
    private fun manifest(): ModelManifest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ModelManifestReader(context.assets).read()
    }

    private fun preprocessor(): ImagePreprocessor = ImagePreprocessor(manifest())

    private fun whiteJpeg(
        width: Int = 1,
        height: Int = 1,
    ): ByteArray = solidJpeg(width, height, Color.WHITE)

    private fun greyJpeg(
        width: Int = 224,
        height: Int = 224,
    ): ByteArray = solidJpeg(width, height, Color.rgb(128, 128, 128))

    private fun gradientJpeg(
        width: Int = 100,
        height: Int = 100,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            val v = (y * 255 / (height - 1)).coerceIn(0, 255)
            for (x in 0 until width) {
                bitmap.setPixel(x, y, Color.rgb(v, v, v))
            }
        }
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        return out.toByteArray()
    }

    private fun solidJpeg(
        width: Int,
        height: Int,
        color: Int,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        return out.toByteArray()
    }

    @Test
    fun preprocessOutputsManifestInputSize() {
        val result = preprocessor().preprocess(whiteJpeg())
        val expected = manifest().inputSize
        assertThat(result.width).isEqualTo(expected)
        assertThat(result.height).isEqualTo(expected)
    }

    @Test
    fun preprocessOutputsThreeChannelFloatBuffer() {
        val result = preprocessor().preprocess(greyJpeg())
        // [1, H, W, 3] interleaved as a contiguous float array
        val expectedSize = manifest().inputSize * manifest().inputSize * 3
        assertThat(result.normalisedRgb.size).isEqualTo(expectedSize)
    }

    @Test
    fun whitePixelNormalisesToOne() {
        val result = preprocessor().preprocess(whiteJpeg())
        val sample = result.normalisedRgb[0]
        // (255 - 127.5) / 127.5 = 1.0
        assertThat(sample).isWithin(0.01f).of(1.0f)
    }

    @Test
    fun midGreyPixelNormalisesToZero() {
        // 224x224 solid mid-grey (128) → after normalize with mean=127.5, std=127.5, every value ≈ 0.004
        val result = preprocessor().preprocess(greyJpeg())
        val sample = result.normalisedRgb[0]
        assertThat(sample).isWithin(0.01f).of(0.0f)
    }

    @Test
    fun gradientJpegProducesNonConstantTensor() {
        // The 100x100 gradient bitmap should produce a tensor with at least two distinct values.
        val result = preprocessor().preprocess(gradientJpeg())
        val sample = result.normalisedRgb.toSet()
        assertThat(sample.size).isGreaterThan(1)
    }

    @Test
    fun emptyJpegBytesThrowIdentificationFailure() {
        // BitmapFactory.decodeByteArray returns null on an empty array; the preprocessor
        // converts this null into an IdentificationFailureException so the camera flow
        // can route to a clean "retake" state rather than treating model-input breakage
        // as low confidence (PLANTPOTTING-0003 §4.3).
        try {
            preprocessor().preprocess(ByteArray(0))
            assertThat("did not throw").isEqualTo("threw IdentificationFailureException")
        } catch (e: com.darkfactory.plantpotting.identify.IdentificationFailureException) {
            assertThat(e.message).isNotEmpty()
        }
    }
}
