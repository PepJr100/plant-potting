package com.darkfactory.plantpotting.credits

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0011 — confirms the Image credits screen renders its title and an attribution row
 * for a CC-BY image (the legal requirement), reading the bundled `image_credits.tsv`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class CreditsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun rendersTitleAndACcByAttributionRow() {
        composeRule.setContent { CreditsScreen(onHome = {}) }
        composeRule.onNodeWithTag(CreditsScreenTags.TITLE).assertIsDisplayed()
        composeRule
            .onNodeWithTag(CreditsScreenTags.rowTag("dracaena"))
            .performScrollTo()
            .assertIsDisplayed()
    }
}
