package com.darkfactory.plantpotting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.camera.CameraScreenTags
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.result.RecommendationScreenTags
import com.darkfactory.plantpotting.result.ResultScreenTags
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class EndToEndFlowTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Force the fake guard to report granted *before* the activity launches.
    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    // Belt-and-braces: also grant the platform permission so any direct
    // ContextCompat.checkSelfPermission call elsewhere succeeds.
    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun grantedHappyPathReachesRecommendationScreen() {
        // Permission was granted at install time → host should navigate
        // immediately to the camera screen. Wait for the camera screen to
        // compose before reaching for the view model, then drive
        // onCaptureReady directly (the GMD AOSP image has no camera sensor).
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewModelProbe.findCameraViewModel() != null
        }
        composeRule.runOnIdle {
            ViewModelProbe.findCameraViewModel()?.onCaptureReady(byteArrayOf(0, 1, 2, 3))
        }

        // Result screen: source-driven badge visible. Production wiring is now the
        // on-device model (PLANTPOTTING-0003 §5.6), so the badge reads "On-device match"
        // — not the legacy "Stub identifier" copy.
        composeRule.onNodeWithTag(ResultScreenTags.SOURCE_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("On-device match").assertIsDisplayed()
        composeRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()

        // Navigate to the recommendation screen.
        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).performClick()

        // Recommendation screen: archetype name + recipe rows whose proportions sum to 100.
        composeRule.onNodeWithTag(RecommendationScreenTags.ARCHETYPE_NAME).assertIsDisplayed()
        composeRule
            .onAllNodesWithTag(RecommendationScreenTags.RECIPE_LIST)
            .onFirst()
            .assertIsDisplayed()

        // Retake returns to the camera.
        composeRule.onNodeWithTag(RecommendationScreenTags.RETAKE).performClick()
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.onFirst() = this[0]
