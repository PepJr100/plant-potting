package com.darkfactory.plantpotting.identify

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.darkfactory.plantpotting.identify.model.ImagePreprocessor
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelLabelsReader
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.darkfactory.plantpotting.identify.model.ModelScoreMapper
import com.darkfactory.plantpotting.identify.model.PreprocessMode
import com.darkfactory.plantpotting.identify.model.TfLiteInterpreterFacade
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * PLANTPOTTING-0011 Phase 1c — the honest accuracy scorecard harness.
 *
 * For the **production model only** (`house_plant_species_mobilenetv2`), runs every clean fixture
 * AND every [FixturePerturbations] copy of it through each candidate **preprocessing mode**
 * (`squash` = the shipping control, `center_crop`, and `tta5` = 5-crop TTA over center_crop) and
 * the production `ModelScoreMapper`. It emits one rich per-row CSV (`accuracy-eval.csv`) plus an
 * aggregate `accuracy-eval-summary.md` to the device external files dir + logcat.
 *
 * This single rich CSV is deliberately the input to BOTH later phases, so one GMD run produces all
 * the evidence:
 *  - Phase 2 (preprocessing ADOPT/DROP) compares top-1 across `mode` (threshold-independent).
 *  - Phase 3 (abstention tuning) sweeps thresholds offline against the raw `margin` / route columns.
 *
 * The `squash` rows at the shipped thresholds are the committed **BEFORE** number. Perturbation rows
 * are *synthetic-robustness*, never real-world samples. GMD `pixel6Api34` is CI-only.
 *
 * Pull after a run (see docs/sprints/evidence/PLANTPOTTING-0011/README.md):
 *   adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/accuracy-eval.csv
 *   adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/accuracy-eval-summary.md
 */
@HiltAndroidTest
class AccuracyEvalTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var kb: KnowledgeBase

    @Inject lateinit var appAssets: AssetManager

    @Before fun init() = hiltRule.inject()

    private val testAssets
        get() = InstrumentationRegistry.getInstrumentation().context.assets

    private data class Mode(
        val name: String,
        val preprocessMode: PreprocessMode,
        val tta: Int,
    )

    private val modes =
        listOf(
            Mode("squash", PreprocessMode.SQUASH, 1),
            Mode("center_crop", PreprocessMode.CENTER_CROP, 1),
            Mode("tta6", PreprocessMode.SQUASH, 6),
        )

    private data class Row(
        val baseImage: String,
        val expectedId: String,
        val mode: String,
        val perturbation: String,
        val family: String,
        val rawTop1Label: String,
        val rawTop1Score: Float,
        val rawTop2Label: String,
        val rawTop2Score: Float,
        val margin: Float,
        val mappedTop1: String,
        val mappedTop3: List<String>,
        val inVocab: Boolean,
        val route: String,
        val confidentWrong: Boolean,
        val latencyMs: Long,
        val failure: String,
    )

    private fun fixtureFiles(): List<String> =
        testAssets
            .list("identify-fixtures")
            .orEmpty()
            .filter { it.endsWith(".jpg", ignoreCase = true) }
            .sorted()

    @Test
    fun scoreProductionModelOverFixturesAndPerturbationsEmitCsv() {
        val root = "ml/$PRODUCTION_MODEL"
        assertThat(appAssets.list("ml")?.toList().orEmpty()).contains(PRODUCTION_MODEL)
        assertThat(appAssets.list(root)?.contains("model.tflite")).isTrue()

        val manifest = ModelManifestReader(appAssets, root).read()
        val labels = ModelLabelsReader(appAssets, "$root/labels.csv").read()
        val mapping = ModelLabelMapReader(appAssets, root).read()
        val facade =
            TfLiteInterpreterFacade(
                assets = appAssets,
                modelPath = "$root/model.tflite",
                labelCount = manifest.labelCount,
                inputSize = manifest.inputSize,
                expectedInputDtype = manifest.inputDtype,
            )
        val mapper =
            ModelScoreMapper(
                labels = labels,
                mapping = mapping,
                kb = kb,
                thresholds = manifest.thresholds,
                perSpeciesThresholds = manifest.perSpeciesThresholds,
                // PLANTPOTTING-0012 — exercise the production pothos↔Pilea boundary gate in the eval.
                boundaryPairs = manifest.boundaryPairs,
            )

        fun inVocab(kbId: String): Boolean = labels.any { mapping.lookup(it)?.kbSpeciesId == kbId }

        // One preprocessor per mode (the manifest copy carries the lever settings).
        val preprocessors =
            modes.associateWith { m ->
                ImagePreprocessor(manifest.copy(preprocessMode = m.preprocessMode, ttaCropCount = m.tta))
            }

        val rows = mutableListOf<Row>()
        try {
            val fixtures = fixtureFiles()
            assertThat(fixtures).isNotEmpty()

            // Warm the interpreter so the first timed row isn't a cold-start outlier.
            val warmBytes = testAssets.open("identify-fixtures/${fixtures.first()}").use { it.readBytes() }
            val warmPre = preprocessors.values.first().preprocessVariants(warmBytes)
            repeat(3) { warmPre.forEach { facade.runInference(it) } }

            for (fixture in fixtures) {
                val baseImage = fixture.removeSuffix(".jpg").removeSuffix(".JPG")
                val expectedId = baseImage.substringBefore("__")
                val cleanBytes = testAssets.open("identify-fixtures/$fixture").use { it.readBytes() }
                val cleanBitmap = BitmapFactory.decodeByteArray(cleanBytes, 0, cleanBytes.size)

                // (perturbationName, family, jpegBytes) — clean uses original bytes; perturbations
                // are derived bitmaps re-encoded to JPEG so they traverse the real decode path.
                val inputs =
                    buildList {
                        add(Triple("clean", "clean", cleanBytes))
                        FixturePerturbations.all(cleanBitmap).forEach {
                            add(Triple(it.kind, it.family, encodeJpeg(it.bitmap)))
                        }
                    }

                for ((perturbation, family, jpeg) in inputs) {
                    for ((mode, preprocessor) in preprocessors) {
                        rows +=
                            scoreRow(
                                baseImage = baseImage,
                                expectedId = expectedId,
                                modeName = mode.name,
                                perturbation = perturbation,
                                family = family,
                                jpeg = jpeg,
                                preprocessor = preprocessor,
                                facade = facade,
                                mapper = mapper,
                                labels = labels,
                                inVocab = inVocab(expectedId),
                            )
                    }
                }
            }
        } finally {
            facade.close()
        }

        writeCsv(rows, "accuracy-eval.csv")
        writeSummary(rows, "accuracy-eval-summary.md")

        // Sanity guards (not accuracy assertions — those live in the committed scorecard):
        // every row scored cleanly, and the schema includes the load-bearing columns.
        assertThat(rows).isNotEmpty()
        assertThat(rows.none { it.failure.isNotEmpty() }).isTrue()
        assertThat(rows.map { it.mode }.toSet()).containsExactly("squash", "center_crop", "tta6")
    }

    /**
     * PLANTPOTTING-0012 Phase 7 — TTA level sweep (×6/×8/×10/×20) on the GATED production pipeline.
     * Experiment-only: it does NOT change the shipped `model_manifest.json` `tta`; it runs the real
     * preprocessor at higher crop counts via `manifest.copy(ttaCropCount = …)` and emits
     * `tta-sweep.csv` (+ summary) for the adopt/keep decision. Gated behind `-e ttaSweep true` so the
     * default CI `androidTest` run (and the scorecard test above) never pays the ×20 cost.
     */
    @Test
    fun sweepTtaLevelsEmitCsv() {
        val args = InstrumentationRegistry.getArguments()
        org.junit.Assume.assumeTrue(
            "TTA sweep runs only with -e ttaSweep true",
            args.getString("ttaSweep") == "true",
        )
        val root = "ml/$PRODUCTION_MODEL"
        val manifest = ModelManifestReader(appAssets, root).read()
        val labels = ModelLabelsReader(appAssets, "$root/labels.csv").read()
        val mapping = ModelLabelMapReader(appAssets, root).read()
        val facade =
            TfLiteInterpreterFacade(
                assets = appAssets,
                modelPath = "$root/model.tflite",
                labelCount = manifest.labelCount,
                inputSize = manifest.inputSize,
                expectedInputDtype = manifest.inputDtype,
            )
        val mapper =
            ModelScoreMapper(
                labels = labels,
                mapping = mapping,
                kb = kb,
                thresholds = manifest.thresholds,
                perSpeciesThresholds = manifest.perSpeciesThresholds,
                boundaryPairs = manifest.boundaryPairs, // gated pipeline
            )

        fun inVocab(kbId: String): Boolean = labels.any { mapping.lookup(it)?.kbSpeciesId == kbId }

        // PLANTPOTTING-0012 Phase 7 — grid-tiling sweep: base 6 (control), +2×2 grid (10), +2×2+3×3 grid (19).
        val sweepModes =
            listOf(
                Mode("tta6", PreprocessMode.SQUASH, 6),
                Mode("grid2x2", PreprocessMode.SQUASH, 10),
                Mode("grid2x2_3x3", PreprocessMode.SQUASH, 19),
            )
        val preprocessors =
            sweepModes.associateWith { m ->
                ImagePreprocessor(manifest.copy(preprocessMode = m.preprocessMode, ttaCropCount = m.tta))
            }

        val rows = mutableListOf<Row>()
        try {
            val fixtures = fixtureFiles()
            val warmBytes = testAssets.open("identify-fixtures/${fixtures.first()}").use { it.readBytes() }
            val warmPre = preprocessors.values.first().preprocessVariants(warmBytes)
            repeat(3) { warmPre.forEach { facade.runInference(it) } }
            for (fixture in fixtures) {
                val baseImage = fixture.removeSuffix(".jpg").removeSuffix(".JPG")
                val expectedId = baseImage.substringBefore("__")
                val cleanBytes = testAssets.open("identify-fixtures/$fixture").use { it.readBytes() }
                val cleanBitmap = BitmapFactory.decodeByteArray(cleanBytes, 0, cleanBytes.size)
                val inputs =
                    buildList {
                        add(Triple("clean", "clean", cleanBytes))
                        FixturePerturbations.all(cleanBitmap).forEach { add(Triple(it.kind, it.family, encodeJpeg(it.bitmap))) }
                    }
                for ((perturbation, family, jpeg) in inputs) {
                    for ((mode, preprocessor) in preprocessors) {
                        rows +=
                            scoreRow(
                                baseImage = baseImage,
                                expectedId = expectedId,
                                modeName = mode.name,
                                perturbation = perturbation,
                                family = family,
                                jpeg = jpeg,
                                preprocessor = preprocessor,
                                facade = facade,
                                mapper = mapper,
                                labels = labels,
                                inVocab = inVocab(expectedId),
                            )
                    }
                }
            }
        } finally {
            facade.close()
        }
        writeCsv(rows, "tta-sweep.csv")
        writeSummary(rows, "tta-sweep-summary.md")
        assertThat(rows).isNotEmpty()
        assertThat(rows.none { it.failure.isNotEmpty() }).isTrue()
        assertThat(rows.map { it.mode }.toSet()).containsExactly("tta6", "grid2x2", "grid2x2_3x3")
    }

    private fun scoreRow(
        baseImage: String,
        expectedId: String,
        modeName: String,
        perturbation: String,
        family: String,
        jpeg: ByteArray,
        preprocessor: ImagePreprocessor,
        facade: TfLiteInterpreterFacade,
        mapper: ModelScoreMapper,
        labels: List<String>,
        inVocab: Boolean,
    ): Row =
        try {
            val variants = preprocessor.preprocessVariants(jpeg)
            val t0 = System.nanoTime()
            val scores = averageScores(variants.map { facade.runInference(it) })
            val latency = (System.nanoTime() - t0) / 1_000_000

            val ranked = scores.indices.sortedByDescending { scores[it] }
            val top1 = ranked[0]
            val top2 = if (ranked.size > 1) ranked[1] else top1
            val margin = scores[top1] - scores[top2]
            val mapped = mapper.map(scores)
            val mappedTop1 =
                if (!mapped.result.lowConfidence) mapped.result.speciesId else mapped.candidates.firstOrNull()?.speciesId ?: ""
            val route = if (mapped.result.lowConfidence) "low-conf" else "high-conf"
            val confidentWrong = route == "high-conf" && mapped.result.speciesId != expectedId
            Row(
                baseImage = baseImage,
                expectedId = expectedId,
                mode = modeName,
                perturbation = perturbation,
                family = family,
                rawTop1Label = labels[top1],
                rawTop1Score = scores[top1],
                rawTop2Label = labels[top2],
                rawTop2Score = scores[top2],
                margin = margin,
                mappedTop1 = mappedTop1,
                mappedTop3 = mapped.candidates.take(3).map { it.speciesId },
                inVocab = inVocab,
                route = route,
                confidentWrong = confidentWrong,
                latencyMs = latency,
                failure = "",
            )
        } catch (e: Throwable) {
            Row(
                baseImage,
                expectedId,
                modeName,
                perturbation,
                family,
                "",
                0f,
                "",
                0f,
                0f,
                "",
                emptyList(),
                inVocab,
                "error",
                false,
                -1,
                e.message ?: e.javaClass.simpleName,
            )
        }

    private fun averageScores(vectors: List<FloatArray>): FloatArray {
        if (vectors.size == 1) return vectors[0]
        val acc = FloatArray(vectors[0].size)
        for (v in vectors) for (i in acc.indices) acc[i] += v[i]
        val n = vectors.size.toFloat()
        for (i in acc.indices) acc[i] /= n
        return acc
    }

    private fun encodeJpeg(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        return out.toByteArray()
    }

    private fun writeCsv(
        rows: List<Row>,
        fileName: String,
    ) {
        val sb = StringBuilder()
        sb.append(
            "base_image,expected_species_id,mode,perturbation,family,raw_top1_label,raw_top1_score," +
                "raw_top2_label,raw_top2_score,margin,mapped_top1_kb_id,mapped_top3_kb_ids,in_vocab," +
                "route,confident_wrong,latency_ms,failure\n",
        )
        for (r in rows) {
            sb.append(
                listOf(
                    r.baseImage,
                    r.expectedId,
                    r.mode,
                    r.perturbation,
                    r.family,
                    csv(r.rawTop1Label),
                    "%.4f".format(r.rawTop1Score),
                    csv(r.rawTop2Label),
                    "%.4f".format(r.rawTop2Score),
                    "%.4f".format(r.margin),
                    r.mappedTop1,
                    csv(r.mappedTop3.joinToString("|")),
                    r.inVocab,
                    r.route,
                    r.confidentWrong,
                    r.latencyMs,
                    csv(r.failure),
                ).joinToString(","),
            )
            sb.append("\n")
        }
        val out = File(targetFilesDir(), fileName)
        out.writeText(sb.toString())
        Log.i(TAG, "Wrote ${rows.size} rows to ${out.absolutePath}")
        sb.toString().lineSequence().forEach { Log.i(TAG, it) }
    }

    private fun writeSummary(
        rows: List<Row>,
        fileName: String,
    ) {
        val sb = StringBuilder()
        sb.append("# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)\n\n")
        sb.append("Production model: `$PRODUCTION_MODEL`. Perturbation rows are *synthetic-robustness*.\n\n")
        for (mode in rows.map { it.mode }.distinct()) {
            sb.append("## mode: $mode\n\n")
            val modeRows = rows.filter { it.mode == mode && it.failure.isEmpty() }
            // Overall + each family (clean is its own "family").
            val families =
                listOf("clean") +
                    rows
                        .map { it.family }
                        .filter { it != "clean" }
                        .distinct()
                        .sorted()
            sb.append("| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |\n")
            sb.append("|---|---|---|---|---|---|---|---|\n")
            sb.append(metricLine("ALL", modeRows))
            for (fam in families) sb.append(metricLine(fam, modeRows.filter { it.family == fam }))
            sb.append(metricLine("IN-VOCAB (all)", modeRows.filter { it.inVocab }))
            // Per-base-image-averaged top-1 (clean rows only) so one heavily-perturbed photo can't dominate.
            val perBaseClean =
                modeRows
                    .filter { it.perturbation == "clean" }
                    .groupBy { it.baseImage }
                    .map { (_, rs) -> if (rs.any { it.mappedTop1 == it.expectedId && it.route == "high-conf" }) 1.0 else 0.0 }
            val perBaseTop1 = if (perBaseClean.isEmpty()) 0.0 else perBaseClean.average()
            sb.append("\nPer-base-image-averaged clean top-1 (high-conf correct): ${"%.3f".format(perBaseTop1)}\n\n")
        }
        File(targetFilesDir(), fileName).writeText(sb.toString())
        sb.toString().lineSequence().forEach { Log.i(TAG, it) }
    }

    private fun metricLine(
        label: String,
        rs: List<Row>,
    ): String {
        if (rs.isEmpty()) return "| $label | 0 | - | - | - | - | - | - |\n"
        val n = rs.size
        val top1 = rs.count { it.route == "high-conf" && it.mappedTop1 == it.expectedId }.toDouble() / n
        val top3 = rs.count { it.expectedId in it.mappedTop3 }.toDouble() / n
        val confWrong = rs.count { it.confidentWrong }.toDouble() / n
        val abstain = rs.count { it.route == "low-conf" }.toDouble() / n
        val lats = rs.map { it.latencyMs }.filter { it >= 0 }.sorted()
        val med = if (lats.isEmpty()) -1 else lats[lats.size / 2]
        val worst = lats.maxOrNull() ?: -1
        return "| $label | $n | ${"%.3f".format(top1)} | ${"%.3f".format(top3)} | " +
            "${"%.3f".format(confWrong)} | ${"%.3f".format(abstain)} | $med | $worst |\n"
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
        private const val TAG = "AccuracyEval"
        private const val PRODUCTION_MODEL = "house_plant_species_mobilenetv2"
    }
}
