package com.darkfactory.plantpotting.identify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.identify.model.ImagePreprocessor
import com.darkfactory.plantpotting.identify.model.InterpreterFacade
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelLabelsReader
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.darkfactory.plantpotting.identify.model.ModelScoreMapper
import com.darkfactory.plantpotting.identify.model.PreprocessedImage
import com.darkfactory.plantpotting.identify.model.TfLiteInterpreterFacade
import com.darkfactory.plantpotting.kb.KbLoader
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

/**
 * PLANTPOTTING-0003 §3.11 — hard-failure path is *not* a low-confidence emission.
 *
 * Two failure modes covered:
 *  - Model load failure (real `TfLiteInterpreterFacade` pointed at a missing asset)
 *  - Native inference failure (an `InterpreterFacade` that throws during runInference)
 *
 * Both must surface as [IdentificationFailureException] and never as `lowConfidence = true`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class OnDevicePlantIdentifierFailureTest {
    @Test
    fun nativeInferenceFailureSurfacesAsIdentificationFailureNotLowConfidence() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val manifest = ModelManifestReader(context.assets).read()
            val labels = ModelLabelsReader(context.assets).read()
            val mapping = ModelLabelMapReader(context.assets).read()
            val kb = KbLoader(context.assets).load()
            val mapper =
                ModelScoreMapper(
                    labels = labels,
                    mapping = mapping,
                    kb = kb,
                    thresholds = manifest.thresholds,
                )
            val throwing =
                object : InterpreterFacade {
                    override val labelCount = manifest.labelCount

                    override fun runInference(input: PreprocessedImage): FloatArray = throw RuntimeException("simulated native crash")

                    override fun close() = Unit
                }
            val identifier =
                OnDevicePlantIdentifier(
                    preprocessor = ImagePreprocessor(manifest),
                    facade = throwing,
                    mapper = mapper,
                    dispatcher = Dispatchers.Unconfined,
                )
            try {
                identifier.identify(jpegOf(Color.WHITE))
                assertThat("did not throw").isEqualTo("threw IdentificationFailureException")
            } catch (e: IdentificationFailureException) {
                assertThat(e.message).contains("On-device identifier failed")
                assertThat(e.cause).isInstanceOf(RuntimeException::class.java)
            }
        }

    @Test
    fun modelLoadFailureFromMissingAssetSurfacesAsIdentificationFailure() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val manifest = ModelManifestReader(context.assets).read()
            val labels = ModelLabelsReader(context.assets).read()
            val mapping = ModelLabelMapReader(context.assets).read()
            val kb = KbLoader(context.assets).load()
            val mapper =
                ModelScoreMapper(
                    labels = labels,
                    mapping = mapping,
                    kb = kb,
                    thresholds = manifest.thresholds,
                )
            val brokenFacade =
                TfLiteInterpreterFacade(
                    assets = context.assets,
                    modelPath = "ml/aiy_plants_v1/no_such_model.tflite",
                    labelCount = manifest.labelCount,
                    inputSize = manifest.inputSize,
                    expectedInputDtype = manifest.inputDtype,
                )
            val identifier =
                OnDevicePlantIdentifier(
                    preprocessor = ImagePreprocessor(manifest),
                    facade = brokenFacade,
                    mapper = mapper,
                    dispatcher = Dispatchers.Unconfined,
                )
            try {
                identifier.identify(jpegOf(Color.WHITE))
                assertThat("did not throw").isEqualTo("threw IdentificationFailureException")
            } catch (e: IdentificationFailureException) {
                // The error must propagate, not be swallowed into a lowConfidence verdict.
                assertThat(e.message).isNotEmpty()
            }
        }

    @Test
    fun emptyJpegSurfacesAsIdentificationFailureNotLowConfidence() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val manifest = ModelManifestReader(context.assets).read()
            val labels = ModelLabelsReader(context.assets).read()
            val mapping = ModelLabelMapReader(context.assets).read()
            val kb = KbLoader(context.assets).load()
            val mapper =
                ModelScoreMapper(
                    labels = labels,
                    mapping = mapping,
                    kb = kb,
                    thresholds = manifest.thresholds,
                )
            val identifier =
                OnDevicePlantIdentifier(
                    preprocessor = ImagePreprocessor(manifest),
                    facade =
                        object : InterpreterFacade {
                            override val labelCount = manifest.labelCount

                            override fun runInference(input: PreprocessedImage): FloatArray = FloatArray(labelCount) { 0f }

                            override fun close() = Unit
                        },
                    mapper = mapper,
                    dispatcher = Dispatchers.Unconfined,
                )
            try {
                identifier.identify(ByteArray(0))
                assertThat("did not throw").isEqualTo("threw")
            } catch (e: IdentificationFailureException) {
                assertThat(e.message).isNotEmpty()
            }
        }

    private fun jpegOf(color: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        return out.toByteArray()
    }
}
