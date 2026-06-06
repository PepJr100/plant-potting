package com.darkfactory.plantpotting.permission

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.camera.CameraScreenTags
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.startIdentifyFromHome
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation regression test for Bug 4 (PLANTPOTTING-0002 §1.1). The user
 * deny → Settings round-trip lands back in the app via `MainActivity.onResume`,
 * which is the signal [PermissionScreenHost] must observe to re-check
 * [CameraPermissionGuard.isGranted].
 *
 * The fake guard starts ungranted so the navhost stays on the permission
 * screen. The test then flips the guard to granted (simulating the user
 * toggling the permission in App Info) and drives the activity through
 * `STARTED → RESUMED` so the new lifecycle-event observer fires. The
 * acceptance signal is the appearance of [CameraScreenTags.SHUTTER] without a
 * process restart.
 *
 * Notes:
 * - We deliberately do *not* drive the system permission dialog. Instead we
 *   manipulate the fake guard directly — the production code's only sensitive
 *   coupling is `CameraPermissionGuard.isGranted()`, and that is exactly what
 *   the fake controls.
 * - The lifecycle dance is via `ActivityScenario.moveToState`, which mirrors a
 *   real Settings round-trip closely enough for this regression.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class PermissionResumeRecoveryTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceUngranted = FakeGuardStateRule(granted = false, shouldShowRationale = false)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        FakeCameraPermissionGuard.reset()
        FakeCameraPermissionGuard.grantedOverride = false
        FakeCameraPermissionGuard.shouldShowRationaleOverride = false
    }

    @After
    fun tearDown() {
        FakeCameraPermissionGuard.reset()
    }

    @Test
    fun resumeAfterSystemSettingsGrantNavigatesToCameraWithoutProcessRestart() {
        // From Home, tap Identify → permission screen shown because the guard reports ungranted.
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(PermissionScreenTags.GRANT_BUTTON).assertIsDisplayed()

        // Simulate the Settings round-trip: while the app is paused, the user
        // grants the permission. We flip the fake guard so that the next call
        // to `isGranted()` returns true.
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        FakeCameraPermissionGuard.grantedOverride = true

        // Returning to the foreground must fire `ON_RESUME` on the
        // PermissionScreenHost's lifecycle owner, prompting it to re-check
        // the guard and navigate to the camera screen.
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(CameraScreenTags.SHUTTER).isNotEmpty()
        }
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()

        // The permission screen's button must be gone — popUpTo(permission,
        // inclusive = true) on the nav controller removes it from the back
        // stack, which is also the no-double-fire safeguard for onGranted.
        check(composeRule.onAllNodesWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).isEmpty()) {
            "OPEN_SETTINGS_BUTTON still attached after resume — onGranted may have leaked back into the host"
        }
        check(composeRule.onAllNodesWithTag(PermissionScreenTags.GRANT_BUTTON).isEmpty()) {
            "GRANT_BUTTON still attached after resume — nav did not pop the permission screen"
        }
    }
}

private fun SemanticsNodeInteractionCollection.isNotEmpty(): Boolean = fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()

private fun SemanticsNodeInteractionCollection.isEmpty(): Boolean = fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty()
