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
 * Decision §4.5: we inject the **concrete** class, not the `PlantIdentifier` interface.
 * `TestIdentifyModule` (`@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])`)
 * swaps the `@Binds PlantIdentifier` only; the concrete `@Singleton @Inject`
 * `OnDevicePlantIdentifier` is still buildable from the binding graph and gets the real
 * [com.darkfactory.plantpotting.identify.model.ImagePreprocessor] +
 * [com.darkfactory.plantpotting.identify.model.TfLiteInterpreterFacade] from
 * [com.darkfactory.plantpotting.identify.OnDeviceIdentifyProvidersModule].
 *
 * Falsifiability (§4.6): if the §1.5 preprocessor branch is reverted to FLOAT32, this
 * test fails with the verbatim Bug 1 error
 * (`"Cannot convert between a TensorFlowLite tensor with type UINT8 …"`).
 */
@HiltAndroidTest
class OnDeviceModelRealInterpreterTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var identifier: OnDevicePlantIdentifier

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
}
