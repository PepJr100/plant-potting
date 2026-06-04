package com.darkfactory.plantpotting.identify

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
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
 * **@Ignore until the converted bundle is installed.** Injection eagerly reads the candidate's
 * `model_manifest.json`, so this test cannot run until
 * `app/src/main/assets/ml/house_plant_species_mobilenetv2/` exists (incl. `model.tflite`). Run
 * the conversion in `docs/sprints/evidence/PLANTPOTTING-0007/ACQUISITION.md`, install the bundle,
 * then **remove `@Ignore`**. Fill the accuracy assertions (TODO below) from the Phase 3
 * `model-swap-eval.csv` — evidence-driven, not pre-committed.
 */
@HiltAndroidTest
@UninstallModules(ActiveModelRootModule::class)
@Ignore("Enable after installing the converted house_plant_species_mobilenetv2 bundle — see ACQUISITION.md")
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
            val covered =
                listOf(
                    "monstera-deliciosa.jpg",
                    "crassula-ovata.jpg",
                    "dracaena-trifasciata.jpg",
                    "epipremnum-aureum.jpg",
                    "zamioculcas-zamiifolia.jpg",
                    "spathiphyllum-wallisii.jpg",
                )
            var highConfHits = 0
            for (name in covered) {
                val result = identifier.identify(fixture(name))
                // Mechanism: production identifier, frozen seam, source unchanged.
                assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
                if (!result.lowConfidence) highConfHits++
            }

            // G4 "not a paper spike": at least 3 of the covered houseplants identify high-confidence
            // through the production path. (This is the live-run acceptance floor; tighten per-species
            // from model-swap-eval.csv once the probe has run.)
            assertThat(highConfHits).isAtLeast(3)

            // TODO(PLANTPOTTING-0007 Phase 4, post-probe): add per-species assertions from
            //   docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval.csv, e.g.
            //   assertThat(identify("dracaena-trifasciata.jpg").speciesId).isEqualTo("dracaena-trifasciata")
            //   for each fixture the probe shows clears the threshold (preferred form), or a
            //   documented top-3 fallback with the deciding number.
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
