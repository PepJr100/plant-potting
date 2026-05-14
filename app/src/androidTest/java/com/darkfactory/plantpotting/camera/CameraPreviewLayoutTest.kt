package com.darkfactory.plantpotting.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.ViewModelProbe
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.result.RecommendationScreenTags
import com.darkfactory.plantpotting.result.ResultScreenTags
import com.google.common.truth.Truth.assertWithMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * PLANTPOTTING-0002 Phase 5 — regression guard for Bug 2 (transient
 * preview letterboxing reported in one Claude Code screenshot but not
 * reproducible in the human replay). The bug, if it exists, would show
 * the preview occupying less than the full Box parent after Retake.
 *
 * This test walks the full §2.1 flow and asserts that
 * `CameraScreenTags.PREVIEW` measures > 80% of its parent on first
 * entry AND on re-entry post-Retake. If this test ever fails, we have
 * ground truth that Bug 2 reproduces — §5.3 then prescribes hoisting
 * `bindCameraUseCases` out of the `AndroidView.factory` into a
 * `LaunchedEffect`.
 */
@HiltAndroidTest
class CameraPreviewLayoutTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun previewFillsMostOfParentOnFirstEntryAndAfterRetake() {
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        assertPreviewFillsParent("first entry")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewModelProbe.findCameraViewModel() != null
        }
        composeRule.runOnIdle {
            ViewModelProbe.findCameraViewModel()?.onCaptureReady(byteArrayOf(0, 1, 2, 3))
        }

        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).assertIsDisplayed()
        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).performClick()

        composeRule.onNodeWithTag(RecommendationScreenTags.RETAKE).assertIsDisplayed()
        composeRule.onNodeWithTag(RecommendationScreenTags.RETAKE).performClick()

        // Back on the camera screen.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag(CameraScreenTags.SHUTTER)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        assertPreviewFillsParent("after Retake")
    }

    private fun assertPreviewFillsParent(label: String) {
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val previewBounds =
            composeRule
                .onNodeWithTag(CameraScreenTags.PREVIEW)
                .fetchSemanticsNode()
                .boundsInRoot
        val widthRatio: Float = previewBounds.width / rootBounds.width
        val heightRatio: Float = previewBounds.height / rootBounds.height
        assertWithMessage("$label preview width ratio").that(widthRatio).isAtLeast(0.8f)
        assertWithMessage("$label preview height ratio").that(heightRatio).isAtLeast(0.8f)
    }
}
