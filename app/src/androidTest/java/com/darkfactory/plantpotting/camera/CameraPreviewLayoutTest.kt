package com.darkfactory.plantpotting.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.ViewModelProbe
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.startIdentifyFromHome
import com.google.common.truth.Truth.assertWithMessage
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
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
@UninstallModules(OnDeviceIdentifyModule::class)
class CameraPreviewLayoutTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

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
    fun previewFillsMostOfParentOnFirstEntry() {
        // PLANTPOTTING-0010 review: Retake was removed (the recommendation page now has a Home
        // button), so the old "return to camera and re-check" round-trip no longer applies. The
        // first-entry preview-fills-parent invariant is what this test guards.
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewModelProbe.findCameraViewModel() != null
        }
        assertPreviewFillsParent("first entry")
    }

    private fun assertPreviewFillsParent(label: String) {
        // The PreviewView's AndroidView can report stale/zero bounds for a frame or two after the
        // camera view model first appears — its surface + layout settle asynchronously on the GMD
        // AOSP image. Poll until it has been laid out to (near-)full size before asserting, so the
        // invariant isn't raced against an un-measured first frame. This only removes the timing
        // race: a genuinely undersized preview still fails (the waitUntil times out → final assert).
        composeRule.waitUntil(timeoutMillis = 5_000) {
            val ratios = previewToRootRatios() ?: return@waitUntil false
            ratios.first >= 0.8f && ratios.second >= 0.8f
        }
        val (widthRatio, heightRatio) =
            previewToRootRatios() ?: throw AssertionError("$label preview was never laid out")
        assertWithMessage("$label preview width ratio").that(widthRatio).isAtLeast(0.8f)
        assertWithMessage("$label preview height ratio").that(heightRatio).isAtLeast(0.8f)
    }

    /** Preview width/height as a fraction of the root, or null while either is not yet measured. */
    private fun previewToRootRatios(): Pair<Float, Float>? {
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        if (rootBounds.width <= 0f || rootBounds.height <= 0f) return null
        val previewBounds =
            composeRule
                .onNodeWithTag(CameraScreenTags.PREVIEW)
                .fetchSemanticsNode()
                .boundsInRoot
        return (previewBounds.width / rootBounds.width) to (previewBounds.height / rootBounds.height)
    }
}
