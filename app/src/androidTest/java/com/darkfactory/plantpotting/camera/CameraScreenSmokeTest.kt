package com.darkfactory.plantpotting.camera

import androidx.camera.core.ImageCapture
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.startIdentifyFromHome
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Rule
import org.junit.Test

/**
 * Recovery of the deferred §6.7 smoke test from PLANTPOTTING-0001, sharpened
 * by PLANTPOTTING-0002 §2.1 to be the direct regression check for Bug 1 —
 * the shutter must not render the literal "C" glyph from
 * `R.string.camera_shutter_label.first()`.
 *
 * The shutter's enabled-state assertion is exercised via
 * [CameraScreenRegistrySetupRule] which injects an [ImageCapture] BEFORE
 * the activity launches; this avoids depending on whether the emulated
 * back camera on the GMD happens to bind successfully or not.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class CameraScreenSmokeTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 4)
    val registrySetup =
        CameraScreenRegistrySetupRule(
            testImageCaptureFactory = { ImageCapture.Builder().build() },
        )

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @After
    fun clearRegistry() {
        CameraScreenTestRegistry.testImageCapture = null
        CameraScreenTestRegistry.forceSkipBind = false
    }

    @Test
    fun shutterIsRenderedAndDoesNotShowLiteralCGlyph() {
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val label = context.getString(R.string.camera_shutter_label)
        val firstChar = label.first().toString()

        // Bug 1 regression: no text node may equal the first character of
        // `camera_shutter_label` (or the literal "C" even if the label is
        // reworded). The full label is allowed only as a content
        // description, not as visible text.
        assertNoTextNodeEquals(firstChar)
        if (firstChar != "C") {
            assertNoTextNodeEquals("C")
        }
        composeRule.onNodeWithContentDescription(label).assertIsDisplayed()
    }

    @Test
    fun shutterIsEnabledWhenIdleAndImageCaptureBound() {
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsEnabled()
    }

    private fun assertNoTextNodeEquals(text: String) {
        val matches =
            composeRule
                .onAllNodesWithText(text, substring = false, ignoreCase = false)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertThat(matches).isEmpty()
    }
}
