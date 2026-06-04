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
 * PLANTPOTTING-0004 §3 — closes the test gap that allowed Bug 1 (PLANTPOTTING-0003 review)
 * to ship. This is the only test in the suite that drives the **real**
 * [OnDevicePlantIdentifier] (production preprocessor + production
 * `TfLiteInterpreterFacade` against the real `model.tflite` asset) end-to-end.
 *
 * PLANTPOTTING-0005 §3 — the global `@TestInstallIn` swap to a fake has been removed;
 * per-test `@BindValue` is the canonical pattern now. The
 * production `OnDeviceIdentifyModule.@Binds PlantIdentifier → OnDevicePlantIdentifier`
 * is therefore active for this test (no @UninstallModules), and the §3.10 guard below
 * asserts it. We continue to also `@Inject` the concrete `OnDevicePlantIdentifier`
 * directly so the test exercises the real native interpreter regardless of the bind.
 *
 * Falsifiability (§4.6): if the §1.5 preprocessor branch is reverted to FLOAT32, this
 * test fails with the verbatim Bug 1 error
 * (`"Cannot convert between a TensorFlowLite tensor with type UINT8 …"`).
 */
@HiltAndroidTest
@UninstallModules(ActiveModelRootModule::class)
class OnDeviceModelRealInterpreterTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    // PLANTPOTTING-0007 — the production default is now the House Plant Species MobileNetV2.
    // This test is the AIY baseline regression anchor (Monstera 0.8984 high-conf, jade 0.1055
    // low-conf), so it pins `ACTIVE_MODEL_ROOT` to the frozen AIY bundle regardless of the
    // shipped default. The candidate's own behaviour is covered by ModelSwapEvaluationTest and
    // OnDeviceModelAppWiredPrototypeTest.
    @BindValue @ActiveModelRoot
    val activeModelRoot: String = "ml/aiy_plants_v1"

    @Inject lateinit var identifier: OnDevicePlantIdentifier

    // PLANTPOTTING-0005 §3.10 production-shape regression guard. Without this, a
    // future global swap (a re-introduced global @TestInstallIn replacement)
    // could silently downgrade this test's `PlantIdentifier` binding back to a fake
    // while leaving the concrete `identifier` field above pointing at the real one.
    @Inject lateinit var boundIdentifier: PlantIdentifier

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun realMonsteraPhotoRoutesHighConfidenceToMonstera() =
        runBlocking {
            val ctx = InstrumentationRegistry.getInstrumentation().context
            val bytes =
                ctx.assets
                    .open("identify-fixtures/monstera-deliciosa.jpg")
                    .use { it.readBytes() }

            val result = identifier.identify(bytes)

            // PLANTPOTTING-0005 §5.6 — accuracy-bearing assertion against the real
            // CC-licensed Monstera deliciosa fixture (§5.4). The §5.5 GMD probe measured
            // top-1 = monstera-deliciosa at p=0.8984, clearing the 0.55 global threshold
            // cleanly, so the *preferred* form applies: direct species id + high confidence.
            // (Falsifiability §4.6: reverting the §1.5 UINT8 preprocessor branch to FLOAT32
            // fails this test with the verbatim Bug 1 "Cannot convert … UINT8" error.)
            assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
            assertThat(result.speciesId).isEqualTo("monstera-deliciosa")
            assertThat(result.lowConfidence).isFalse()
            assertThat(identifier::class.java).isEqualTo(OnDevicePlantIdentifier::class.java)
        }

    @Test
    fun plantIdentifierBindingResolvesToOnDevicePlantIdentifier() {
        assertThat(boundIdentifier::class.qualifiedName)
            .isEqualTo("com.darkfactory.plantpotting.identify.OnDevicePlantIdentifier")
    }

    @Test
    fun realCrassulaPhotoRanksJadeTopMappedButRoutesLowConfidence() =
        runBlocking {
            val ctx = InstrumentationRegistry.getInstrumentation().context
            val bytes =
                ctx.assets
                    .open("identify-fixtures/crassula-ovata.jpg")
                    .use { it.readBytes() }

            val result = identifier.identify(bytes)
            val candidates = identifier.mostRecentCandidates

            // PLANTPOTTING-0006 §Phase 2 — accuracy-bearing assertion against the real
            // CC0 Crassula ovata fixture (§Phase 1), DOCUMENTED HONEST FALLBACK form.
            // The §Phase 2 GMD probe measured the top *mapped* candidate as
            // crassula-ovata at p ≈ 0.1055 (monstera-deliciosa ≈ 0.0000) — far below the
            // global high_confidence_plain = 0.55 and below margin_min = 0.45 — so the
            // photo routes LOW-CONFIDENCE, NOT the preferred high-confidence-direct form.
            // The AIY V1/3 vocabulary contains "Crassula ovata" but the model does not
            // confidently recognise this canonical jade photo as that class (contrast the
            // Monstera fixture at 0.8984). Seeding a per-species threshold to rescue a
            // ~10%-confidence prediction would be egregious overfitting (§5.6 prohibition),
            // so per_species_thresholds ships empty — see docs/kb/ml-mapping-notes.md and
            // docs/sprints/evidence/PLANTPOTTING-0006/probe-outcome.md.
            assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
            assertThat(result.lowConfidence).isTrue()
            assertThat(result.speciesId).isEmpty()
            // The mapping + scoring wiring is exercised end-to-end: among the two in-vocab
            // mapped KB species, the real jade photo ranks crassula-ovata first.
            assertThat(candidates).isNotEmpty()
            assertThat(candidates.first().speciesId).isEqualTo("crassula-ovata")
        }
}
