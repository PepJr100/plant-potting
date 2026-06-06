package com.darkfactory.plantpotting.identify

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * PLANTPOTTING-0011 Phase 1a — license discipline for the instrumentation `identify-fixtures/`.
 * Mirrors [com.darkfactory.plantpotting.result.ReferenceImageManifestTest] for the eval fixtures.
 *
 * Every `identify-fixtures` JPEG must have a row in `fixture-manifest.tsv`, and any fixture added
 * this sprint must declare a **CC0 / public-domain** license. Four pre-existing CC BY-SA fixtures
 * (including the AIY baseline anchor `monstera-deliciosa.jpg`, which cannot be swapped) are
 * GRANDFATHERED by the explicit allowlist below — the test fails if any OTHER fixture is non-CC0/PD,
 * so a new CC-BY/CC-BY-SA fixture can never slip in (the 0011 non-goal).
 *
 * Runs with the module dir (`app/`) as working directory.
 */
class FixtureLicenseManifestTest {
    private val fixturesDir = File("src/androidTest/assets/identify-fixtures")
    private val manifestFile = File("src/androidTest/assets/identify-fixtures/fixture-manifest.tsv")

    /** Pre-0011 CC BY-SA fixtures, allowed to keep their license. No additions permitted. */
    private val grandfathered =
        setOf(
            "monstera-deliciosa.jpg",
            "spathiphyllum-wallisii.jpg",
            "phalaenopsis.jpg",
            "goeppertia-orbifolia.jpg",
        )

    private fun jpgFixtures(): List<String> =
        fixturesDir
            .listFiles { f -> f.isFile && f.name.endsWith(".jpg", ignoreCase = true) }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

    private data class Row(
        val filename: String,
        val expectedId: String,
        val license: String,
    )

    private fun manifestRows(): Map<String, Row> {
        val lines =
            manifestFile
                .readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
        // First non-comment line is the header.
        val header = lines.first().split("\t").map { it.trim() }
        val iFile = header.indexOf("filename")
        val iId = header.indexOf("expected_species_id")
        val iLic = header.indexOf("license")
        check(iFile >= 0 && iId >= 0 && iLic >= 0) { "manifest header missing required columns: $header" }
        return lines
            .drop(1)
            .map { it.split("\t") }
            .associate { cols ->
                cols[iFile].trim() to Row(cols[iFile].trim(), cols[iId].trim(), cols[iLic].trim())
            }
    }

    private fun isCc0OrPublicDomain(license: String): Boolean =
        Regex("^(CC0|Public domain|PD)\\b", RegexOption.IGNORE_CASE).containsMatchIn(license.trim())

    @Test
    fun manifestFileExists() {
        assertWithMessage("fixture manifest at ${manifestFile.absolutePath}")
            .that(manifestFile.exists())
            .isTrue()
    }

    @Test
    fun everyFixtureHasAManifestRow() {
        val rows = manifestRows()
        for (name in jpgFixtures()) {
            assertWithMessage("fixture '$name' must have a row in fixture-manifest.tsv")
                .that(rows.containsKey(name))
                .isTrue()
        }
    }

    @Test
    fun noOrphanManifestRows() {
        val files = jpgFixtures().toSet()
        for (name in manifestRows().keys) {
            assertWithMessage("manifest row '$name' has no matching fixture file")
                .that(files.contains(name))
                .isTrue()
        }
    }

    @Test
    fun newFixturesAreCc0OrPublicDomain() {
        val rows = manifestRows()
        for (name in jpgFixtures()) {
            if (name in grandfathered) continue
            val lic = rows[name]?.license.orEmpty()
            assertWithMessage("fixture '$name' must be CC0/public-domain (declared: '$lic')")
                .that(isCc0OrPublicDomain(lic))
                .isTrue()
        }
    }

    @Test
    fun onlyTheKnownFourFixturesMayBeNonCc0() {
        // Prevents a new CC-BY/CC-BY-SA fixture from being smuggled in under any naming.
        val rows = manifestRows()
        val nonClean = jpgFixtures().filter { !isCc0OrPublicDomain(rows[it]?.license.orEmpty()) }.toSet()
        assertThat(nonClean).isEqualTo(grandfathered.intersect(jpgFixtures().toSet()))
    }
}
