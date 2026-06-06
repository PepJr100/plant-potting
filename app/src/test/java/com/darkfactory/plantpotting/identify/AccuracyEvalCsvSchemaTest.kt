package com.darkfactory.plantpotting.identify

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * PLANTPOTTING-0011 Phase 1c — schema guard for the committed accuracy scorecard.
 *
 * The scorecard CSV (`docs/sprints/evidence/PLANTPOTTING-0011/accuracy-eval.csv`, produced by the
 * GMD `AccuracyEvalTest` and committed as the BEFORE/AFTER evidence) is read by humans AND by the
 * offline preprocessing/threshold analyses. A future edit that silently drops the **`margin`** or
 * **`confident_wrong`** columns would quietly break those analyses. This test fails loudly if any
 * load-bearing column disappears from the header.
 *
 * Runs with the module dir (`app/`) as the working directory, so the evidence file is one level up.
 */
class AccuracyEvalCsvSchemaTest {
    private val csv = File("../docs/sprints/evidence/PLANTPOTTING-0011/accuracy-eval.csv")

    private val requiredColumns =
        listOf(
            "base_image",
            "expected_species_id",
            "mode",
            "perturbation",
            "family",
            "margin",
            "mapped_top1_kb_id",
            "mapped_top3_kb_ids",
            "in_vocab",
            "route",
            "confident_wrong",
            "latency_ms",
        )

    @Test
    fun scorecardCsvExists() {
        assertWithMessage("accuracy-eval.csv at ${csv.absolutePath}")
            .that(csv.exists())
            .isTrue()
    }

    @Test
    fun headerRetainsEveryLoadBearingColumn() {
        val header = csv.useLines { it.firstOrNull() }.orEmpty()
        val columns = header.split(",").map { it.trim() }.toSet()
        for (col in requiredColumns) {
            assertWithMessage("scorecard CSV header must keep the '$col' column (found: $columns)")
                .that(columns.contains(col))
                .isTrue()
        }
    }
}
