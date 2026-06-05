package com.darkfactory.plantpotting.camera

import androidx.compose.ui.test.assertIsDisplayed
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
 * PLANTPOTTING-0010 D5 — the debug-only theme-switcher affordance is gated by `showDebugAffordances`
 * (defaulting to `BuildConfig.DEBUG`). This pins the *exclusion* logic deterministically: with the
 * flag false (release), the entry is absent; with it true, it's present and fires its callback.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class CameraDebugAffordanceTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun themeSwitcherEntryAbsentWhenDebugAffordancesOff() {
        composeRule.setContent {
            CameraScreen(
                viewModel = CameraViewModel(StubIdentifier()),
                onNavigate = {},
                showDebugAffordances = false,
            )
        }
        composeRule.onNodeWithTag(CameraScreenTags.THEME_SWITCHER_ENTRY).assertDoesNotExist()
    }

    @Test
    fun themeSwitcherEntryPresentAndClickableWhenDebugAffordancesOn() {
        var opened = false
        composeRule.setContent {
            CameraScreen(
                viewModel = CameraViewModel(StubIdentifier()),
                onNavigate = {},
                onOpenThemeSwitcher = { opened = true },
                showDebugAffordances = true,
            )
        }
        composeRule.onNodeWithTag(CameraScreenTags.THEME_SWITCHER_ENTRY).assertIsDisplayed().performClick()
        assertThat(opened).isTrue()
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
