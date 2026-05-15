package com.darkfactory.plantpotting.camera

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.identify.IdentificationResult
import com.darkfactory.plantpotting.identify.PlantIdentifier
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0005 §0.8 — contract-lock test. RED today (no `camera.failureBanner`
 * node exists on [CameraScreen]); flips GREEN after §2.3 / §2.4 add the
 * `CameraScreenTags.FAILURE_BANNER` tag (= `"camera.failureBanner"`).
 *
 * Drives the view-model into `CameraUiState.Failure("test")` via
 * `onCaptureFailed`, then asserts the banner node exists on the screen.
 * Literal tag string used here so the JVM compile keeps working between Phase 0
 * (contract land) and Phase 2 (constant introduced). The literal must match the
 * §2.3 constant value verbatim.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class CameraFailureBannerContractTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun failureStateRendersBannerNode() {
        val vm = CameraViewModel(StubIdentifier())
        vm.onCaptureFailed("test")
        composeRule.setContent {
            CameraScreen(viewModel = vm, onNavigate = {})
        }
        composeRule.onNodeWithTag("camera.failureBanner").assertExists()
    }

    private class StubIdentifier : PlantIdentifier {
        override suspend fun identify(jpeg: ByteArray): IdentificationResult =
            IdentificationResult(
                speciesId = "",
                displayName = "",
                source = IdSource.STUB_DETERMINISTIC,
                lowConfidence = false,
            )
    }
}
