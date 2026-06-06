package com.darkfactory.plantpotting.result

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * PLANTPOTTING-0010 Phase 6 / D4 — license discipline for bundled reference imagery. Every WebP under
 * `res/drawable-nodpi/` must have a row in `docs/licenses/reference-images.md`, so a CC0/PD image can
 * never ship without a recorded source + license. Tolerant of an absent dir (none bundled yet).
 */
class ReferenceImageManifestTest {
    private val drawableDir = File("src/main/res/drawable-nodpi")
    private val manifest = File("../docs/licenses/reference-images.md")

    private fun bundledWebpNames(): List<String> =
        drawableDir
            .listFiles { f -> f.isFile && f.name.endsWith(".webp") }
            ?.map { it.name }
            ?: emptyList()

    @Test
    fun manifestFileExists() {
        assertWithMessage("attribution manifest at ${manifest.absolutePath}")
            .that(manifest.exists())
            .isTrue()
    }

    @Test
    fun everyBundledReferenceImageHasAManifestEntry() {
        val webps = bundledWebpNames()
        if (webps.isEmpty()) return // placeholder-only: vacuously satisfied
        val manifestText = manifest.readText()
        for (name in webps) {
            assertWithMessage("reference image '$name' must have a row in reference-images.md")
                .that(manifestText.contains(name))
                .isTrue()
        }
    }

    @Test
    fun manifestDeclaresCc0OrPublicDomainPolicy() {
        // Guards the policy line so a future edit can't silently drop the CC0/PD baseline.
        val text = manifest.readText().lowercase()
        assertThat(text.contains("cc0") || text.contains("public-domain") || text.contains("public domain"))
            .isTrue()
    }

    @Test
    fun manifestForbidsCcBySa() {
        // PLANTPOTTING-0011 relaxed the policy to allow CC BY (with attribution) but NOT CC BY-SA.
        // The policy text must still document the share-alike exclusion.
        val text = manifest.readText().lowercase()
        assertThat(text.contains("cc by-sa is still not accepted") || text.contains("not accepted"))
            .isTrue()
    }
}
