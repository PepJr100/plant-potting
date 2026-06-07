package com.darkfactory.plantpotting.credits

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * PLANTPOTTING-0011 — attribution discipline for the CC-BY relaxation. Every reference image
 * licensed **CC BY** (in `docs/licenses/reference-images.md`) MUST have an in-app attribution row in
 * the bundled `image_credits.tsv` (CC BY's legal requirement), and **no** reference image may be
 * CC BY-SA (still not accepted). CC0/PD images need no credit.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ImageCreditsTest {
    private val manifest = File("../docs/licenses/reference-images.md")

    private data class Row(
        val drawable: String,
        val license: String,
    )

    /** Parse the `| file.webp | species | License | Source |` table rows. */
    private fun manifestRows(): List<Row> =
        manifest.readLines()
            .filter { it.trimStart().startsWith("|") && it.contains(".webp") }
            .mapNotNull { line ->
                val cols = line.split("|").map { it.trim() }
                // cols[0] is empty (leading pipe); [1]=file [2]=species [3]=license
                if (cols.size < 4) return@mapNotNull null
                val file = cols[1]
                if (!file.endsWith(".webp")) return@mapNotNull null
                Row(drawable = file.removeSuffix(".webp"), license = cols[3])
            }

    private fun credits(): List<ImageCredit> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return loadImageCredits(context.assets)
    }

    @Test
    fun noReferenceImageIsCcBySa() {
        val sa = manifestRows().filter { it.license.contains("CC BY-SA", ignoreCase = true) }
        assertWithMessage("CC BY-SA reference images are not accepted: $sa").that(sa).isEmpty()
    }

    /** Licenses that require (CC BY) or merit (Unsplash, Pexels) an in-app credit. CC0/PD need none. */
    private fun needsCredit(license: String): Boolean =
        Regex("^CC BY \\d", RegexOption.IGNORE_CASE).containsMatchIn(license) ||
            license.equals("Unsplash License", ignoreCase = true) ||
            license.equals("Pexels License", ignoreCase = true)

    @Test
    fun everyCreditMeritingImageHasAnInAppCreditRow() {
        val needing = manifestRows().filter { needsCredit(it.license) }.map { it.drawable }.toSet()
        val credited = credits().map { it.drawable }.toSet()
        val missing = needing - credited
        assertWithMessage("CC BY / Unsplash images missing an image_credits.tsv attribution row")
            .that(missing)
            .isEmpty()
    }

    @Test
    fun everyCreditRowIsWellFormedAndPointsAtACreditMeritingImage() {
        val byDrawable = manifestRows().associate { it.drawable to it.license }
        val rows = credits()
        assertThat(rows).isNotEmpty()
        for (c in rows) {
            assertWithMessage("credit '${c.drawable}' author").that(c.author).isNotEmpty()
            assertWithMessage("credit '${c.drawable}' license").that(c.license).isNotEmpty()
            assertWithMessage("credit '${c.drawable}' source url").that(c.sourceUrl).startsWith("http")
            assertWithMessage("credit '${c.drawable}' must correspond to a CC BY / Unsplash manifest row")
                .that(byDrawable[c.drawable]?.let { needsCredit(it) })
                .isTrue()
        }
    }
}
