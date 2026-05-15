package com.darkfactory.plantpotting.identify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.identify.model.FakeInterpreterFacade
import com.darkfactory.plantpotting.identify.model.ImagePreprocessor
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelLabelsReader
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.darkfactory.plantpotting.identify.model.ModelScoreMapper
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
 * PLANTPOTTING-0003 §3.8 — end-to-end behaviour of the on-device identifier pipeline.
 *
 * Real ImagePreprocessor + real ModelScoreMapper + a `FakeInterpreterFacade` seeded per
 * test. Bundled JPEG bytes are generated in-memory at fixture build time so the test
 * stays self-contained and reproducible (the AIY V1 real model is the §2 Blocker; even
 * if we shipped real JPEGs they would not run through TFLite under Robolectric).
 *
 * Observed top-1 / top-3 *probability* numbers are not asserted — they are evidence
 * material per §3.8's plan note, not acceptance gates.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class OnDevicePlantIdentifierFixturesTest {
    private fun pipeline(scores: FloatArray): OnDevicePlantIdentifier {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manifest = ModelManifestReader(context.assets).read()
        val labels = ModelLabelsReader(context.assets).read()
        val mapping = ModelLabelMapReader(context.assets).read()
        val kb = runBlocking { KbLoader(context.assets).load() }
        val mapper =
            ModelScoreMapper(
                labels = labels,
                mapping = mapping,
                kb = kb,
                thresholds = manifest.thresholds,
            )
        require(scores.size == manifest.labelCount) {
            "Test seed has ${scores.size} scores but manifest declares ${manifest.labelCount}"
        }
        val fakeFacade =
            FakeInterpreterFacade(
                labelCount = manifest.labelCount,
                cannedScores = scores,
            )
        return OnDevicePlantIdentifier(
            preprocessor = ImagePreprocessor(manifest),
            facade = fakeFacade,
            mapper = mapper,
            dispatcher = Dispatchers.Unconfined,
        )
    }

    private fun monsteraDeliciosaJpeg(): ByteArray = jpegOf(Color.rgb(40, 90, 30), 200, 200)

    private fun ficusLyrataJpeg(): ByteArray = jpegOf(Color.rgb(60, 100, 40), 200, 200)

    private fun blankGreyJpeg(): ByteArray = jpegOf(Color.rgb(128, 128, 128), 200, 200)

    private fun jpegOf(
        color: Int,
        width: Int,
        height: Int,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        return out.toByteArray()
    }

    /** Labels are loaded in the same order as labels.csv — fixed by the asset. */
    private fun scoresWithBest(
        labelIndex: Int,
        bestProb: Float,
        labelCount: Int,
    ): FloatArray =
        FloatArray(labelCount).also { arr ->
            arr[labelIndex] = bestProb
            val remainder = (1f - bestProb) / (labelCount - 1)
            for (i in arr.indices) if (i != labelIndex) arr[i] = remainder
        }

    @Test
    fun monsteraFixtureRoutesToMonsteraDeliciosaSpeciesId() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val labelCount = ModelManifestReader(context.assets).read().labelCount
            val labels = ModelLabelsReader(context.assets).read()
            val monsteraIdx = labels.indexOf("Monstera deliciosa")
            assertThat(monsteraIdx).isGreaterThan(-1)
            val identifier = pipeline(scoresWithBest(monsteraIdx, 0.80f, labelCount))
            val result = identifier.identify(monsteraDeliciosaJpeg())
            assertThat(result.speciesId).isEqualTo("monstera-deliciosa")
            assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
            assertThat(result.lowConfidence).isFalse()
        }

    @Test
    fun ficusFixtureRoutesToFicusLyrataSpeciesId() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val labelCount = ModelManifestReader(context.assets).read().labelCount
            val labels = ModelLabelsReader(context.assets).read()
            val ficusIdx = labels.indexOf("Ficus lyrata")
            assertThat(ficusIdx).isGreaterThan(-1)
            val identifier = pipeline(scoresWithBest(ficusIdx, 0.72f, labelCount))
            val result = identifier.identify(ficusLyrataJpeg())
            assertThat(result.speciesId).isEqualTo("ficus-lyrata")
            assertThat(result.lowConfidence).isFalse()
        }

    @Test
    fun blankGreyFixtureRoutesToLowConfidenceWithEmptyCandidates() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val labelCount = ModelManifestReader(context.assets).read().labelCount
            // Uniform near-zero distribution — argmax probability well below 0.45.
            val flat = FloatArray(labelCount) { 1f / labelCount }
            val identifier = pipeline(flat)
            val result = identifier.identify(blankGreyJpeg())
            assertThat(result.lowConfidence).isTrue()
            assertThat(result.speciesId).isEmpty()
            // The 18 labels are all mapped today, so the up-to-3 list would normally fill;
            // this fixture exists to prove the pipeline reaches the low-confidence emission
            // path on a clearly uncertain distribution.
            assertThat(identifier.mostRecentCandidates).hasSize(3)
        }

    @Test
    fun mostRecentCandidatesAreClearedOnFailure() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val labelCount = ModelManifestReader(context.assets).read().labelCount
            val labels = ModelLabelsReader(context.assets).read()
            val monsteraIdx = labels.indexOf("Monstera deliciosa")
            val identifier = pipeline(scoresWithBest(monsteraIdx, 0.80f, labelCount))
            // First call populates candidates.
            identifier.identify(monsteraDeliciosaJpeg())
            assertThat(identifier.mostRecentCandidates).isNotEmpty()
            // Force a hard failure (empty bytes → preprocessor rejects).
            try {
                identifier.identify(ByteArray(0))
            } catch (_: IdentificationFailureException) {
                // expected
            }
            assertThat(identifier.mostRecentCandidates).isEmpty()
        }
}
