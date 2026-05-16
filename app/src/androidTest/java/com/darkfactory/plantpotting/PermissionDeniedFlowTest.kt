package com.darkfactory.plantpotting

import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.permission.PermissionScreenTags
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * The denied path: the fake [com.darkfactory.plantpotting.permission.CameraPermissionGuard]
 * is forced to report ungranted so the navhost stays on the permission screen
 * regardless of the platform-level CAMERA grant state (which can leak in
 * from [EndToEndFlowTest]'s `GrantPermissionRule` on a shared GMD run).
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class PermissionDeniedFlowTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceUngranted = FakeGuardStateRule(granted = false)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun permissionScreenIsShownAtStartup() {
        composeRule.onNodeWithTag(PermissionScreenTags.GRANT_BUTTON).assertIsDisplayed()
    }
}

/**
 * PLANTPOTTING-0005 §4.5 / §4.6 — recovers the previously `@Ignore`d test that
 * asserted the "Open Settings" intent fires on permanent denial. The original
 * `@Ignore` cited that the system permission dialog is outside the Compose tree;
 * the fix is to drive the permanent-denial state via the fake guard instead
 * (see [FakeCameraPermissionGuard.permanentlyDeniedOverride] and the new
 * `CameraPermissionGuard.isPermanentlyDenied()` hook consulted by
 * `PermissionScreenHost`). No system dialog is touched — we assert app behaviour
 * under a known permission state.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class PermissionPermanentlyDeniedFlowTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forcePermanentlyDenied =
        FakeGuardStateRule(granted = false, permanentlyDenied = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun openSettingsIntentFiresOnPermanentDenial() {
        // Screen must surface the permanent-denied state directly (no system dialog).
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).performClick()
        Intents.intended(allOf(hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)))
    }
}
