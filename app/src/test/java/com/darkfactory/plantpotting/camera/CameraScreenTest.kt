package com.darkfactory.plantpotting.camera

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.identify.IdentificationResult
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0005 §2.1 — banner + retry behaviour for `CameraUiState.Failure`.
 *
 * `failureStateRendersBanner` — drives state to `Failure("test reason")` and
 * asserts the polished bottom-anchored banner exists (FAILURE_BANNER tag) and
 * preserves the back-compat `ERROR` tag on the body text.
 *
 * `failureBannerRetryClickResetsState` — clicks the FAILURE_RETRY button and
 * asserts the view-model flips back to `Idle`. This is the persistent-banner
 * affordance the old top-center text didn't have.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class CameraScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun failureStateRendersBanner() {
        val vm = CameraViewModel(StubIdentifier())
        vm.onCaptureFailed("test reason")
        composeRule.setContent {
            CameraScreen(viewModel = vm, onNavigate = {})
        }
        composeRule.onNodeWithTag(CameraScreenTags.FAILURE_BANNER).assertExists()
        composeRule.onNodeWithTag(CameraScreenTags.ERROR).assertExists()
        composeRule.onNodeWithTag(CameraScreenTags.FAILURE_RETRY).assertExists()
    }

    @Test
    fun failureBannerRetryClickResetsState() {
        val vm = CameraViewModel(StubIdentifier())
        vm.onCaptureFailed("test reason")
        composeRule.setContent {
            CameraScreen(viewModel = vm, onNavigate = {})
        }
        composeRule.onNodeWithTag(CameraScreenTags.FAILURE_RETRY).performClick()
        assertThat(vm.state.value).isInstanceOf(CameraUiState.Idle::class.java)
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
