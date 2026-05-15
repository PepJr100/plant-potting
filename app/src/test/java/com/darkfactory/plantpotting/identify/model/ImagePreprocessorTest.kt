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
import java.nio.ByteOrder

/**
 * PLANTPOTTING-0004 §1.4 — verifies `ImagePreprocessor` honors the manifest input contract,
 * branching `TensorImage(DataType.UINT8)` vs `TensorImage(DataType.FLOAT32)` and applying
 * `NormalizeOp` only on the FLOAT32 path. Reads the bundled `model_manifest.json` for the
 * shipped path; constructs in-memory manifests for the FLOAT32 cross-check.
 *
 * The §1.5 refactor lands the dtype branch; without it, the UINT8 assertions fail because
 * the preprocessor would still construct `TensorImage(DataType.FLOAT32)`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ImagePreprocessorTest {
    private fun shippedManifest(): ModelManifest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ModelManifestReader(context.assets).read()
    }

    private fun float32Manifest(inputSize: Int = 224): ModelManifest =
        ModelManifest(
            variant = "test/float32",
            sha256 = "0",
            placeholder = false,
            inputSize = inputSize,
            inputDtype = ModelDtype.FLOAT32,
            colorOrder = "RGB",
            normalization =
                ModelManifest.Normalization(
                    mean = floatArrayOf(127.5f, 127.5f, 127.5f),
                    std = floatArrayOf(127.5f, 127.5f, 127.5f),
                ),
            outputTensorShape = listOf(1, 10),
            labelCount = 10,
            labelsAsset = "stub",
            mappingAsset = "stub",
            thresholds =
                ModelManifest.Thresholds(
                    highConfidencePlain = 0.55f,
                    highConfidenceMarginMin = 0.45f,
                    highConfidenceMarginDelta = 0.18f,
                    topKCandidates = 3,
                ),
        )

    private fun uint8Manifest(inputSize: Int = 224): ModelManifest =
        float32Manifest(inputSize).copy(
            variant = "test/uint8",
            inputDtype = ModelDtype.UINT8,
        )

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
        val manifest = shippedManifest()
        val result = ImagePreprocessor(manifest).preprocess(whiteJpeg())
        assertThat(result.width).isEqualTo(manifest.inputSize)
        assertThat(result.height).isEqualTo(manifest.inputSize)
    }

    @Test
    fun uint8ManifestProducesUint8BufferAndSkipsNormaliseOp() {
        val manifest = uint8Manifest()
        val result = ImagePreprocessor(manifest).preprocess(whiteJpeg())
        // UINT8: one byte per channel.
        assertThat(result.buffer.capacity()).isEqualTo(manifest.inputSize * manifest.inputSize * 3)

        val bytes = result.buffer.duplicate().apply { rewind() }
        // White pixel in → bytes ~= 0xFF (no normalization).
        val sample = bytes.get().toInt() and 0xFF
        assertThat(sample).isAtLeast(0xF0)
    }

    @Test
    fun float32ManifestKeepsNormaliseOp() {
        val manifest = float32Manifest()
        val result = ImagePreprocessor(manifest).preprocess(whiteJpeg())
        // FLOAT32: four bytes per channel.
        assertThat(result.buffer.capacity()).isEqualTo(manifest.inputSize * manifest.inputSize * 3 * 4)

        val floats =
            result.buffer
                .duplicate()
                .apply {
                    order(ByteOrder.nativeOrder())
                    rewind()
                }.asFloatBuffer()
        val firstChannel = floats.get(0)
        // (255 - 127.5) / 127.5 = 1.0
        assertThat(firstChannel).isWithin(0.05f).of(1.0f)
    }

    @Test
    fun gradientJpegProducesNonConstantTensor() {
        val manifest = shippedManifest()
        val result = ImagePreprocessor(manifest).preprocess(gradientJpeg())
        val bytes = ByteArray(result.buffer.capacity())
        result.buffer
            .duplicate()
            .apply { rewind() }
            .get(bytes)
        assertThat(bytes.toSet().size).isGreaterThan(1)
    }

    @Test
    fun emptyJpegBytesThrowIdentificationFailure() {
        val manifest = shippedManifest()
        try {
            ImagePreprocessor(manifest).preprocess(ByteArray(0))
            assertThat("did not throw").isEqualTo("threw IdentificationFailureException")
        } catch (e: com.darkfactory.plantpotting.identify.IdentificationFailureException) {
            assertThat(e.message).isNotEmpty()
        }
    }

    @Test
    fun shippedManifestIsUint8ButGreyImageStillProducesValidBuffer() {
        // Smoke check: shipped manifest is UINT8, so a 224×224 grey image produces a
        // 224*224*3 byte buffer, populated with bytes in [0,255].
        val manifest = shippedManifest()
        assertThat(manifest.inputDtype).isEqualTo(ModelDtype.UINT8)
        val result = ImagePreprocessor(manifest).preprocess(greyJpeg())
        assertThat(result.buffer.capacity()).isEqualTo(manifest.inputSize * manifest.inputSize * 3)
    }
}
