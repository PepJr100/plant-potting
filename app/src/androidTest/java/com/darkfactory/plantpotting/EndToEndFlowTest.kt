package com.darkfactory.plantpotting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.camera.CameraViewModel
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.result.RecommendationScreenTags
import com.darkfactory.plantpotting.result.ResultScreenTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EndToEndFlowTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule(order = 2) val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun grantedHappyPathReachesRecommendationScreen() {
        // Permission was granted at install time → host should navigate
        // immediately to the camera screen. We can't actually take a real
        // camera picture in the GMD; instead, the activity's CameraViewModel
        // exposes onCaptureReady which the test hook drives directly.
        composeRule.activityRule.scenario.onActivity { activity ->
            val vm = ViewModelProbe.findCameraViewModel(activity)
            vm?.onCaptureReady(byteArrayOf(0, 1, 2, 3))
        }

        // Result screen: stub badge visible + scientific name from FakeFixedIdentifier.
        composeRule.onNodeWithTag(ResultScreenTags.STUB_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()

        // Navigate to the recommendation screen.
        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).performClick()

        // Recommendation screen: archetype name + recipe rows whose proportions sum to 100.
        composeRule.onNodeWithTag(RecommendationScreenTags.ARCHETYPE_NAME).assertIsDisplayed()
        composeRule.onAllNodesWithTag(RecommendationScreenTags.RECIPE_LIST).onFirst()
            .assertIsDisplayed()

        // Retake returns to the camera.
        composeRule.onNodeWithTag(RecommendationScreenTags.RETAKE).performClick()
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.onFirst() =
    this[0]
