package com.darkfactory.plantpotting.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0010 D5 — every theme candidate composes (light + dark) without crashing, and the
 * name-resolution falls back safely.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ThemeCandidateTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun everyCandidateComposesInLightAndDark() {
        // setContent is once-per-test, so compose every candidate × light/dark in one tree.
        composeRule.setContent {
            Column {
                for (candidate in ThemeCandidate.entries) {
                    for (dark in listOf(false, true)) {
                        PlantPottingTheme(candidate = candidate, useDarkTheme = dark) {
                            Text("theme-${candidate.name}-$dark")
                        }
                    }
                }
            }
        }
        for (candidate in ThemeCandidate.entries) {
            for (dark in listOf(false, true)) {
                composeRule.onNodeWithText("theme-${candidate.name}-$dark").assertIsDisplayed()
            }
        }
    }

    @Test
    fun fromNameResolvesKnownAndFallsBackToLeaf() {
        assertThat(ThemeCandidate.fromName("TERRACOTTA")).isEqualTo(ThemeCandidate.TERRACOTTA)
        assertThat(ThemeCandidate.fromName("SLATE")).isEqualTo(ThemeCandidate.SLATE)
        assertThat(ThemeCandidate.fromName(null)).isEqualTo(ThemeCandidate.LEAF)
        assertThat(ThemeCandidate.fromName("not-a-theme")).isEqualTo(ThemeCandidate.LEAF)
    }

    @Test
    fun threeCandidatesAreDefined() {
        // The principal picks from 2–3 candidates; assert the count stays in range.
        assertThat(ThemeCandidate.entries.size).isEqualTo(3)
    }
}
