package com.darkfactory.plantpotting.camera

import androidx.camera.core.ImageCapture
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.ViewModelProbe
import com.darkfactory.plantpotting.startIdentifyFromHome
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.result.ResultScreenTags
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Rule
import org.junit.Test

/**
 * PLANTPOTTING-0008 Phase 3 — shutter-on-return regression.
 *
 * The bug: after a capture, [CameraViewModel] advances to
 * [CameraUiState.Success] and that view model survives in the Compose
 * Navigation back-stack-entry's `ViewModelStore`. A `Success` only *navigates*
 * to the result screen (it does not pop the camera entry), so pressing system
 * **back** from the result screen returns to the camera with the state still
 * `Success` → the shutter stays at `alpha 0.5f` / `disabled()` and never
 * re-enables (`CameraScreen.kt` shutter rule: enabled only on `Idle`/`Failure`).
 *
 * This test reproduces the full flow against the real `NavHost`: capture →
 * result → system-back → camera, then asserts the shutter is **enabled** again.
 * It is RED before the lifecycle-aware reset lands in [CameraScreen] and GREEN
 * after.
 *
 * An `ImageCapture` is injected via [CameraScreenRegistrySetupRule] so the
 * assertion isolates the *state* logic (`Idle` vs `Success`) and does not depend
 * on whether the GMD's emulated back camera binds — exactly as the existing
 * bind/bound/smoke tests do.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class CameraShutterOnReturnTest {
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
    fun shutterReEnablesAfterReturningToCameraFromResult() {
        // 1. From Home, tap Identify → permission pre-granted → camera. Back-stack: [Home, Camera].
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsEnabled()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewModelProbe.findCameraViewModel() != null
        }

        // 2. Drive a capture → Success → NavHost pushes the result screen. The
        //    camera back-stack entry (and its Success-state view model) survives.
        composeRule.runOnIdle {
            ViewModelProbe.findCameraViewModel()?.onCaptureReady(byteArrayOf(0, 1, 2, 3))
        }
        composeRule.onNodeWithTag(ResultScreenTags.SOURCE_BADGE).assertIsDisplayed()

        // 3. System-back returns to the (surviving) camera entry — the exact path
        //    that left the shutter stuck disabled before the fix.
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        // 4. Camera is shown again; the shutter must be re-enabled and the
        //    bind-pending overlay must be gone (ImageCapture is bound).
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag(CameraScreenTags.SHUTTER)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsEnabled()
        val bindProgress =
            composeRule
                .onAllNodesWithTag(CameraScreenTags.BIND_PROGRESS)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
        check(bindProgress.isEmpty()) { "BIND_PROGRESS overlay rendered after return-to-camera" }
    }
}
