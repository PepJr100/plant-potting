package com.darkfactory.plantpotting.camera

import androidx.camera.core.ImageCapture
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

/**
 * PLANTPOTTING-0002 Phase 4 (UX 1). The shutter must be disabled and a
 * `BIND_PROGRESS` overlay must be visible while CameraX hasn't yet returned
 * an `ImageCapture`. Once an `ImageCapture` is bound, the overlay must
 * disappear and the shutter must become enabled.
 *
 * The test exercises the two static states by composing the screen with
 * `CameraScreenTestRegistry.testImageCapture` set to `null` (bind-pending)
 * and unset (default, which goes through the real binding path on a GMD
 * with no camera, leaving `imageCapture` null too). To exercise the
 * "bound" state we share the smoke test's fake-injection path; that
 * coverage lives in [CameraScreenSmokeTest.shutterIsEnabledWhenIdleAndImageCaptureBound].
 */
@HiltAndroidTest
class CameraScreenBindStateTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @After
    fun clearRegistry() {
        CameraScreenTestRegistry.testImageCapture = null
    }

    /**
     * Bind-pending: no `ImageCapture` available → shutter disabled and the
     * `BIND_PROGRESS` overlay is rendered. AOSP GMDs have no camera sensor,
     * so the real `bindCameraUseCases` silently fails and leaves
     * `imageCapture = null`, which is exactly the state under test.
     */
    @Test
    fun shutterIsDisabledAndBindProgressVisibleWhileImageCaptureIsNull() {
        // No `testImageCapture` injection.
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsNotEnabled()
        composeRule.onNodeWithTag(CameraScreenTags.BIND_PROGRESS).assertIsDisplayed()
    }

    /**
     * Bound: `ImageCapture` injected → shutter enabled, no `BIND_PROGRESS`.
     */
    @Test
    fun shutterIsEnabledAndBindProgressGoneWhenImageCaptureIsBound() {
        CameraScreenTestRegistry.testImageCapture =
            ImageCapture
                .Builder()
                .build()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsEnabled()
        // BIND_PROGRESS should not exist when imageCapture is non-null.
        val matches =
            composeRule
                .onAllNodesWithTag(CameraScreenTags.BIND_PROGRESS)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
        check(matches.isEmpty()) { "BIND_PROGRESS overlay rendered while ImageCapture was bound" }
    }
}
