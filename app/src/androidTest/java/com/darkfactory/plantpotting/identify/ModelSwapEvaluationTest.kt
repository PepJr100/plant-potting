package com.darkfactory.plantpotting.identify

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.darkfactory.plantpotting.identify.model.ImagePreprocessor
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelLabelsReader
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.darkfactory.plantpotting.identify.model.ModelScoreMapper
import com.darkfactory.plantpotting.identify.model.TfLiteInterpreterFacade
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

/**
 * PLANTPOTTING-0007 §Phase 3 — the swap-evaluation harness.
 *
 * Probes the **AIY baseline** and every present candidate bundle over the expanded
 * `identify-fixtures/` set, feeding each fixture through that model's **own** native
 * preprocessing (the `ModelManifest` dtype/normalization branch), and emits a
 * machine-readable per-fixture CSV (`model-swap-eval.csv`) plus a rough summary to the
 * device's external files dir + logcat. It reuses the production
 * interpreter/preprocessor/mapper/readers via the `ACTIVE_MODEL_ROOT`-style root convention,
 * so it never duplicates that logic.
 *
 * This runs **alongside** [OnDeviceModelRealInterpreterTest] (which stays the binding/seam
 * regression anchor); here we add a model-comparison substrate and pin the AIY baseline rows
 * so a plumbing regression can't hide.
 *
 * Fixtures are named `<kb-species-id>.jpg` (the stem is the expected KB species id). Models
 * are discovered from `assets/ml/<id>/` — AIY is always probed; the houseplant MobileNetV2
 * candidate is probed automatically once its bundle (incl. `model.tflite`) is installed.
 *
 * **Pulling the CSV after a run:**
 *   adb shell run-as com.darkfactory.plantpotting cat files/model-swap-eval.csv   # or
 *   adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/model-swap-eval.csv
 * then copy it to docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval.csv.
 */
@HiltAndroidTest
class ModelSwapEvaluationTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var kb: KnowledgeBase

    // App (main) assets — where the ml/<id>/ bundles live (production AssetManager).
    @Inject lateinit var appAssets: AssetManager

    @Before fun init() = hiltRule.inject()

    private val testAssets
        get() = InstrumentationRegistry.getInstrumentation().context.assets

    private data class ModelUnderTest(
        val modelId: String,
        val root: String,
    )

    private data class Probe(
        val modelId: String,
        val fixtureId: String,
        val expectedId: String,
        val rawTop1Label: String,
        val rawTop1Score: Float,
        val rawTop3: List<Pair<String, Float>>,
        val mappedTop1: String,
        val mappedTop3: List<String>,
        val inVocab: Boolean,
        val route: String,
        val source: String,
        val latencyMs: Long,
        val failure: String,
    )

    /** Models to probe: AIY always; others only if their bundle (with model.tflite) exists. */
    private fun discoverModels(): List<ModelUnderTest> =
        appAssets
            .list("ml")
            ?.toList()
            .orEmpty()
            .sorted()
            .map { ModelUnderTest(modelId = it, root = "ml/$it") }
            .filter { mut -> appAssets.list(mut.root)?.contains("model.tflite") == true }

    private fun fixtureFiles(): List<String> =
        testAssets
            .list("identify-fixtures")
            .orEmpty()
            .filter { it.endsWith(".jpg", ignoreCase = true) }
            .sorted()

    @Test
    fun fixtureReadabilityGuardFailsLoudlyOnMissingOrCorruptImages() {
        val fixtures = fixtureFiles()
        // Required anchors — these MUST exist and decode (the AIY baseline ruler).
        assertThat(fixtures).containsAtLeast("monstera-deliciosa.jpg", "crassula-ovata.jpg")
        for (name in fixtures) {
            val bytes = testAssets.open("identify-fixtures/$name").use { it.readBytes() }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            assertThat(bmp).isNotNull()
            assertThat(bmp.width).isGreaterThan(0)
            assertThat(bmp.height).isGreaterThan(0)
        }
    }

    @Test
    fun probeAllModelsOverFixturesAndEmitCsv() {
        val models = discoverModels()
        assertThat(models.map { it.modelId }).contains("aiy_plants_v1")

        val fixtures = fixtureFiles()
        val rows = mutableListOf<Probe>()
        for (model in models) {
            rows += probeModel(model, fixtures)
        }

        writeCsv(rows)
        writeSummary(rows, models)

        // --- AIY baseline regression anchor (must not silently drift) ---
        val aiyMonstera = rows.single { it.modelId == "aiy_plants_v1" && it.fixtureId == "monstera-deliciosa" }
        assertThat(aiyMonstera.mappedTop1).isEqualTo("monstera-deliciosa")
        assertThat(aiyMonstera.route).isEqualTo("high-conf")
        assertThat(aiyMonstera.rawTop1Score).isWithin(0.03f).of(0.8984f)

        val aiyJade = rows.single { it.modelId == "aiy_plants_v1" && it.fixtureId == "crassula-ovata" }
        assertThat(aiyJade.route).isEqualTo("low-conf")
        assertThat(aiyJade.mappedTop1).isEqualTo("crassula-ovata") // top *mapped* candidate

        // --- Candidate smoke: any non-AIY model present runs clean as ON_DEVICE_MODEL ---
        rows.filter { it.modelId != "aiy_plants_v1" }.forEach {
            assertThat(it.failure).isEmpty()
            assertThat(it.source).isEqualTo("ON_DEVICE_MODEL")
        }

        // --- Evidence-driven candidate assertions (seeded from the 2026-06-05 probe;
        //     see model-swap-eval.csv / -summary.md). Falsifiable: a regression in the model,
        //     mapping, preprocessing, or threshold policy breaks these. ---
        val cand = rows.filter { it.modelId == "house_plant_species_mobilenetv2" }
        if (cand.isNotEmpty()) {
            fun row(fixture: String) = cand.single { it.fixtureId == fixture }

            // Preferred form: correct top-1 at high confidence for these 5 covered species.
            for (f in listOf(
                "monstera-deliciosa",
                "dracaena-trifasciata",
                "goeppertia-orbifolia",
                "phalaenopsis",
                "zamioculcas-zamiifolia",
            )) {
                assertThat(row(f).route).isEqualTo("high-conf")
                assertThat(row(f).mappedTop1).isEqualTo(f)
            }

            // Documented honest fallbacks (reasons in model-swap-eval-summary.md):
            // peace lily is the correct top-1 (spathiphyllum) but at 0.4468 < 0.55 → low-conf
            // (NOT seeded — 0006 anti-overfit discipline).
            assertThat(row("spathiphyllum-wallisii").route).isEqualTo("low-conf")
            assertThat(row("spathiphyllum-wallisii").mappedTop1).isEqualTo("spathiphyllum-wallisii")
            // pothos is confidently confused with Pilea (raw top-1 = Pilea @ 0.9661). PLANTPOTTING-0012
            // mapped Pilea, so without a gate this would now be a confidently-WRONG Pilea card; the
            // pothos↔Pilea boundary gate forces it back to low-conf (picker) — the whole point of 0012.
            assertThat(row("epipremnum-aureum").route).isEqualTo("low-conf")
            // jade is the correct top-1 (crassula-ovata) but the model splits it with money-tree
            // (Pachira) ~0.58/0.42; that <0.30 top1-top2 gap trips the high_confidence_abstain_margin
            // added in PLANTPOTTING-0011 Phase 3, so it honestly routes low-conf. (Pre-0011, with no
            // margin gate, 0.58 > 0.55 made it high-conf — this assertion now tracks the shipped
            // abstention policy, not the stale pre-margin behaviour. NOT seeded — 0006 discipline.)
            assertThat(row("crassula-ovata").route).isEqualTo("low-conf")
            assertThat(row("crassula-ovata").mappedTop1).isEqualTo("crassula-ovata")

            // The candidate must beat AIY on high-confidence correct identifications.
            val aiyHighConf = rows.count { it.modelId == "aiy_plants_v1" && it.route == "high-conf" && it.mappedTop1 == it.expectedId }
            val candHighConf = cand.count { it.route == "high-conf" && it.mappedTop1 == it.expectedId }
            assertThat(candHighConf).isGreaterThan(aiyHighConf)
        }
    }

    private fun probeModel(
        model: ModelUnderTest,
        fixtures: List<String>,
    ): List<Probe> {
        val manifest = ModelManifestReader(appAssets, model.root).read()
        val labels = ModelLabelsReader(appAssets, "${model.root}/labels.csv").read()
        val mapping = ModelLabelMapReader(appAssets, model.root).read()
        val facade =
            TfLiteInterpreterFacade(
                assets = appAssets,
                modelPath = "${model.root}/model.tflite",
                labelCount = manifest.labelCount,
                inputSize = manifest.inputSize,
                expectedInputDtype = manifest.inputDtype,
            )
        val preprocessor = ImagePreprocessor(manifest)
        val mapper =
            ModelScoreMapper(
                labels = labels,
                mapping = mapping,
                kb = kb,
                thresholds = manifest.thresholds,
                perSpeciesThresholds = manifest.perSpeciesThresholds,
                // PLANTPOTTING-0012 — probe the real production mapper config per model (the house-plant
                // model carries the pothos↔Pilea boundary gate; AIY's manifest has none → empty).
                boundaryPairs = manifest.boundaryPairs,
            )

        // Which KB ids are reachable in this model's vocabulary (for the in-vocab flag).
        fun inVocab(kbId: String): Boolean = labels.any { mapping.lookup(it)?.kbSpeciesId == kbId }

        return try {
            fixtures.map { fixture ->
                val expectedId = fixture.removeSuffix(".jpg").removeSuffix(".JPG")
                try {
                    val jpeg = testAssets.open("identify-fixtures/$fixture").use { it.readBytes() }
                    val pre = preprocessor.preprocess(jpeg)

                    // Warm + median-of-5 latency.
                    repeat(2) { facade.runInference(pre) }
                    val timings = LongArray(5)
                    lateinit var scores: FloatArray
                    for (i in 0 until 5) {
                        val t0 = System.nanoTime()
                        scores = facade.runInference(pre)
                        timings[i] = (System.nanoTime() - t0) / 1_000_000
                    }
                    timings.sort()
                    val medianMs = timings[timings.size / 2]

                    val ranked = scores.indices.sortedByDescending { scores[it] }
                    val rawTop3 = ranked.take(3).map { labels[it] to scores[it] }
                    val mapped = mapper.map(scores)
                    val mappedTop1 =
                        if (!mapped.result.lowConfidence) {
                            mapped.result.speciesId
                        } else {
                            mapped.candidates.firstOrNull()?.speciesId ?: ""
                        }
                    Probe(
                        modelId = model.modelId,
                        fixtureId = expectedId,
                        expectedId = expectedId,
                        rawTop1Label = labels[ranked[0]],
                        rawTop1Score = scores[ranked[0]],
                        rawTop3 = rawTop3,
                        mappedTop1 = mappedTop1,
                        mappedTop3 = mapped.candidates.take(3).map { it.speciesId },
                        inVocab = inVocab(expectedId),
                        route = if (mapped.result.lowConfidence) "low-conf" else "high-conf",
                        source = mapped.result.source.name,
                        latencyMs = medianMs,
                        failure = "",
                    )
                } catch (e: Throwable) {
                    Probe(
                        model.modelId,
                        expectedId,
                        expectedId,
                        "",
                        0f,
                        emptyList(),
                        "",
                        emptyList(),
                        false,
                        "error",
                        "",
                        -1,
                        e.message ?: e.javaClass.simpleName,
                    )
                }
            }
        } finally {
            facade.close()
        }
    }

    private fun writeCsv(rows: List<Probe>) {
        val sb = StringBuilder()
        sb.append(
            "model_id,fixture_species_id,expected_species_id,raw_top1_label,raw_top1_score," +
                "raw_top3,mapped_top1_kb_id,mapped_top3_kb_ids,in_vocab,route,source,latency_ms,failure\n",
        )
        for (r in rows) {
            val raw3 = r.rawTop3.joinToString("|") { "${it.first}=${"%.4f".format(it.second)}" }
            val mapped3 = r.mappedTop3.joinToString("|")
            sb.append(
                listOf(
                    r.modelId,
                    r.fixtureId,
                    r.expectedId,
                    csv(r.rawTop1Label),
                    "%.4f".format(r.rawTop1Score),
                    csv(raw3),
                    r.mappedTop1,
                    csv(mapped3),
                    r.inVocab,
                    r.route,
                    r.source,
                    r.latencyMs,
                    csv(r.failure),
                ).joinToString(","),
            )
            sb.append("\n")
        }
        val out = File(targetFilesDir(), "model-swap-eval.csv")
        out.writeText(sb.toString())
        Log.i(TAG, "Wrote ${rows.size} rows to ${out.absolutePath}")
        sb.toString().lineSequence().forEach { Log.i(TAG, it) }
    }

    private fun writeSummary(
        rows: List<Probe>,
        models: List<ModelUnderTest>,
    ) {
        val sb = StringBuilder()
        sb.append("# model-swap-eval summary (raw — polish into model-swap-eval-summary.md)\n\n")
        for (m in models) {
            val mr = rows.filter { it.modelId == m.modelId }
            val top1Correct = mr.count { it.mappedTop1 == it.expectedId && it.route == "high-conf" }
            val top3Correct = mr.count { it.expectedId in it.mappedTop3 }
            val inVocab = mr.count { it.inVocab }
            val medLat =
                mr.map { it.latencyMs }.filter { it >= 0 }.sorted().let {
                    if (it.isEmpty()) -1 else it[it.size / 2]
                }
            sb.append(
                "## ${m.modelId}\n" +
                    "- fixtures probed: ${mr.size}\n" +
                    "- top-1 correct (high-conf): $top1Correct\n" +
                    "- top-3 contains expected: $top3Correct\n" +
                    "- in-vocab fixtures: $inVocab\n" +
                    "- median latency (ms): $medLat\n\n",
            )
        }
        File(targetFilesDir(), "model-swap-eval-summary.md").writeText(sb.toString())
        sb.toString().lineSequence().forEach { Log.i(TAG, it) }
    }

    private fun targetFilesDir(): File =
        InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
            ?: InstrumentationRegistry.getInstrumentation().targetContext.filesDir

    private fun csv(s: String): String =
        if (s.contains(',') || s.contains('"') || s.contains('|')) {
            "\"" + s.replace("\"", "\"\"") + "\""
        } else {
            s
        }

    companion object {
        private const val TAG = "ModelSwapEval"
    }
}
