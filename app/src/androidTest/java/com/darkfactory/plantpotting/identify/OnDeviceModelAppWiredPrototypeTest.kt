package com.darkfactory.plantpotting.identify

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * PLANTPOTTING-0007 §Phase 5 — drives the **app-wired winning model** through the production
 * [OnDevicePlantIdentifier] path (not the bare harness), with `ACTIVE_MODEL_ROOT` pointed at the
 * House Plant Species MobileNetV2 candidate via a per-test Hilt override.
 *
 * `@UninstallModules(ActiveModelRootModule::class)` + `@BindValue` swap the active root to the
 * candidate **for this test only** — every collaborator (manifest/labels/mapping/interpreter)
 * follows the one switch, exactly like a prototype build, while the rest of the suite keeps the
 * frozen AIY default.
 *
 * Enabled 2026-06-05 once the converted bundle was installed. The per-species assertions are
 * seeded from the Phase 3 probe (`model-swap-eval.csv` / `-summary.md`) — evidence-driven, not
 * pre-committed. This is the G4 "not a paper spike" live run: the production identifier, wired to
 * the candidate, identifies real houseplant photos directly.
 */
@HiltAndroidTest
@UninstallModules(ActiveModelRootModule::class)
class OnDeviceModelAppWiredPrototypeTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue @ActiveModelRoot
    val activeModelRoot: String = "ml/house_plant_species_mobilenetv2"

    @Inject lateinit var identifier: OnDevicePlantIdentifier

    @Before fun init() = hiltRule.inject()

    private fun fixture(name: String): ByteArray =
        InstrumentationRegistry
            .getInstrumentation()
            .context.assets
            .open("identify-fixtures/$name")
            .use { it.readBytes() }

    /**
     * Mechanism guarantee: the app-wired candidate path returns `ON_DEVICE_MODEL` for the covered
     * houseplant fixtures, and the seam (`IdentificationResult`) is unchanged. This is the
     * "production path, not the bare harness" check (G4). Accuracy is asserted separately, seeded
     * from the probe.
     */
    @Test
    fun appWiredCandidateIdentifiesHouseplantsThroughProductionPath() =
        runBlocking {
            // Preferred form (probe @ high-conf): correct species id straight from the production
            // identifier. 4 of the 6 covered fixtures clear the threshold cleanly.
            val highConfExpect =
                mapOf(
                    "monstera-deliciosa.jpg" to "monstera-deliciosa",
                    "dracaena-trifasciata.jpg" to "dracaena-trifasciata",
                    "zamioculcas-zamiifolia.jpg" to "zamioculcas-zamiifolia",
                    "crassula-ovata.jpg" to "crassula-ovata",
                )
            for ((file, kbId) in highConfExpect) {
                val result = identifier.identify(fixture(file))
                assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL) // frozen seam, source unchanged
                assertThat(result.lowConfidence).isFalse()
                assertThat(result.speciesId).isEqualTo(kbId)
            }

            // G4 "not a paper spike": ≥3 real houseplants identified high-confidence directly through
            // OnDevicePlantIdentifier. (The 4 above already satisfy it; assert the floor explicitly.)
            assertThat(highConfExpect.size).isAtLeast(3)

            // Documented honest fallback (model-swap-eval-summary.md): peace lily is the correct
            // top-1 but at 0.4468 < 0.55, so it routes low-confidence (not seeded — 0006 discipline).
            val peaceLily = identifier.identify(fixture("spathiphyllum-wallisii.jpg"))
            assertThat(peaceLily.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
            assertThat(peaceLily.lowConfidence).isTrue()
            assertThat(peaceLily.speciesId).isEmpty()
        }

    /**
     * Mechanism guarantee: an out-of-vocab houseplant (not in the candidate's 47 classes) still
     * routes to the existing low-confidence flow — the picker path is unchanged by the swap.
     * `ficus-lyrata` is a KB species absent from the 47-class vocabulary.
     */
    @Test
    fun outOfVocabFixtureStillRoutesLowConfidence() =
        runBlocking {
            // Use a fixture for a KB species the candidate does NOT cover, if present.
            val outOfVocab = "ficus-lyrata.jpg"
            val available =
                InstrumentationRegistry
                    .getInstrumentation()
                    .context.assets
                    .list("identify-fixtures")
                    .orEmpty()
                    .contains(outOfVocab)
            org.junit.Assume.assumeTrue("no out-of-vocab fixture installed yet", available)

            val result = identifier.identify(fixture(outOfVocab))
            assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
            assertThat(result.lowConfidence).isTrue()
            assertThat(result.speciesId).isEmpty()
        }
}
