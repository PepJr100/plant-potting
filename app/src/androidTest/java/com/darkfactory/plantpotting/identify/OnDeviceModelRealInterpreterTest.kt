package com.darkfactory.plantpotting.identify

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
 * PLANTPOTTING-0005 §3 — `TestIdentifyModule` (the global `@TestInstallIn` swap to a
 * fake) has been removed; per-test `@BindValue` is the canonical pattern now. The
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
class OnDeviceModelRealInterpreterTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var identifier: OnDevicePlantIdentifier

    // PLANTPOTTING-0005 §3.10 production-shape regression guard. Without this, a
    // future global swap (a re-introduced `TestIdentifyModule`-style @TestInstallIn)
    // could silently downgrade this test's `PlantIdentifier` binding back to a fake
    // while leaving the concrete `identifier` field above pointing at the real one.
    @Inject lateinit var boundIdentifier: PlantIdentifier

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun realJpegFedThroughRealInterpreterDoesNotThrow() =
        runBlocking {
            val ctx = InstrumentationRegistry.getInstrumentation().context
            val bytes =
                ctx.assets
                    .open("identify-fixtures/monstera-deliciosa.jpg")
                    .use { it.readBytes() }

            val result = identifier.identify(bytes)

            // No assertion on speciesId — AIY V1/3 maps only 2 of 16 KB species verbatim,
            // so the result may legitimately route to lowConfidence = true. The falsifiable
            // claim is "returns without throwing through the real native interpreter" — the
            // exact failure mode of Bug 1.
            assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
            assertThat(identifier::class.java).isEqualTo(OnDevicePlantIdentifier::class.java)
        }

    @Test
    fun plantIdentifierBindingResolvesToOnDevicePlantIdentifier() {
        assertThat(boundIdentifier::class.qualifiedName)
            .isEqualTo("com.darkfactory.plantpotting.identify.OnDevicePlantIdentifier")
    }
}
