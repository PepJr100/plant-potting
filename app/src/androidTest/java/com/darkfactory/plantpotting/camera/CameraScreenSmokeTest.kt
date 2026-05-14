package com.darkfactory.plantpotting.camera

import androidx.camera.core.ImageCapture
import androidx.compose.ui.test.SemanticsMatcher
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
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Recovery of the deferred §6.7 smoke test from PLANTPOTTING-0001, sharpened
 * by PLANTPOTTING-0002 §2.1 to be the direct regression check for Bug 1 —
 * the shutter must not render the literal "C" glyph from
 * `R.string.camera_shutter_label.first()`.
 *
 * Notes:
 * - The AOSP GMD has no camera sensor, so the real CameraX bind in
 *   [bindCameraUseCases] would leave `imageCapture` null. The test injects a
 *   fake `ImageCapture` via [CameraScreenTestRegistry.testImageCapture] so
 *   the shutter's enabled-when-bound branch can be asserted without standing
 *   up CameraX.
 * - The fake `ImageCapture` is built via the public Builder; no camera is
 *   bound to it, which is fine because the test never taps the shutter.
 */
@HiltAndroidTest
class CameraScreenSmokeTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    private lateinit var fakeImageCapture: ImageCapture

    @Before
    fun injectFakeImageCapture() {
        fakeImageCapture =
            ImageCapture
                .Builder()
                .build()
        CameraScreenTestRegistry.testImageCapture = fakeImageCapture
    }

    @After
    fun clearFakeImageCapture() {
        CameraScreenTestRegistry.testImageCapture = null
    }

    @Test
    fun shutterIsRenderedAndDoesNotShowLiteralCGlyph() {
        // Wait until the camera screen composes.
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()

        // Bug 1 regression: no text node anywhere may equal "C" or any other
        // single-character prefix of the shutter label.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val label = context.getString(R.string.camera_shutter_label)
        val firstChar = label.first().toString()

        assertNoTextNodeEquals(firstChar)
        // Be defensive — also assert the literal "C" never appears as a
        // standalone text node, even if `camera_shutter_label` is reworded later.
        if (firstChar != "C") {
            assertNoTextNodeEquals("C")
        }

        // The shutter's content description equals the full string-resource label.
        composeRule.onNodeWithContentDescription(label).assertIsDisplayed()
    }

    @Test
    fun shutterIsEnabledWhenIdleAndImageCaptureBound() {
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

    @Suppress("unused")
    private fun shutterMatcher(): SemanticsMatcher =
        SemanticsMatcher("has shutter test tag") { node ->
            node.config.any { it.key.name == "TestTag" && it.value == CameraScreenTags.SHUTTER }
        }
}
