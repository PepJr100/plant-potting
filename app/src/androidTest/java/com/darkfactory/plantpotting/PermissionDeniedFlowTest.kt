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
import org.junit.Ignore
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

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun permissionScreenIsShownAtStartup() {
        composeRule.onNodeWithTag(PermissionScreenTags.GRANT_BUTTON).assertIsDisplayed()
    }

    @Ignore(
        "Known gap (§8.3 of docs/sprints/results/PLANTPOTTING-0001.md): clicking " +
            "GRANT_BUTTON triggers the system permission dialog, which is outside the " +
            "Compose tree. Reaching the PermanentlyDenied state requires a manual " +
            "deny-twice cycle that automation cannot script.",
    )
    @Test
    fun openSettingsIntentFiresOnPermanentDenial() {
        composeRule.onNodeWithTag(PermissionScreenTags.GRANT_BUTTON).performClick()
        Intents.intended(allOf(hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)))
    }
}
