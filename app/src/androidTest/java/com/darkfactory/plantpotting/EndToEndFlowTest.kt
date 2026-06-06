package com.darkfactory.plantpotting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

        // Result screen: wait for it to compose (navigation is async), then verify the source badge.
        // The screen is scrollable (hero image), so scroll the badge into view before asserting.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag(ResultScreenTags.SOURCE_BADGE)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(ResultScreenTags.SOURCE_BADGE).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("On-device match").assertExists()
        composeRule.onNodeWithText("Monstera deliciosa").assertExists()

        // Navigate to the recommendation screen (the CTA may be below the fold).
        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).performScrollTo().performClick()

        // Recommendation screen: archetype name + recipe rows whose proportions sum to 100.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag(RecommendationScreenTags.ARCHETYPE_NAME)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(RecommendationScreenTags.ARCHETYPE_NAME).performScrollTo().assertIsDisplayed()
        composeRule
            .onAllNodesWithTag(RecommendationScreenTags.RECIPE_LIST)
            .onFirst()
            .assertExists()

        // The Home button (replaces Retake) returns to the landing screen.
        composeRule.onNodeWithTag(RecommendationScreenTags.HOME_BUTTON).performScrollTo().performClick()
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.onFirst() = this[0]
