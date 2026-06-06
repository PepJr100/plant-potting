package com.darkfactory.plantpotting.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.result.MyPlantRow
import com.darkfactory.plantpotting.ui.theme.PlantPottingTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0010 (review feedback / Dribbble layout) — the redesigned landing screen renders the
 * four action tiles + recent-plants carousel and fires the right callbacks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class HomeScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private fun content(
        recent: List<MyPlantRow> = emptyList(),
        onIdentify: () -> Unit = {},
        onMyPlants: () -> Unit = {},
        onBrowseMixes: () -> Unit = {},
        onAbout: () -> Unit = {},
        onRecentClick: (MyPlantRow) -> Unit = {},
    ) {
        composeRule.setContent {
            PlantPottingTheme {
                HomeScreen(
                    recentPlants = recent,
                    onIdentify = onIdentify,
                    onMyPlants = onMyPlants,
                    onBrowseMixes = onBrowseMixes,
                    onAbout = onAbout,
                    onRecentClick = onRecentClick,
                )
            }
        }
    }

    @Test
    fun rendersFourTilesAndEmptyRecentState() {
        content()
        composeRule.onNodeWithTag(HomeTags.IDENTIFY).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.MY_PLANTS).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.BROWSE_MIXES).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.ABOUT).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.RECENT_EMPTY).assertIsDisplayed()
    }

    @Test
    fun tilesFireTheirCallbacks() {
        var identify = false
        var myPlants = false
        var browse = false
        var about = false
        content(
            onIdentify = { identify = true },
            onMyPlants = { myPlants = true },
            onBrowseMixes = { browse = true },
            onAbout = { about = true },
        )
        composeRule.onNodeWithTag(HomeTags.IDENTIFY).performClick()
        composeRule.onNodeWithTag(HomeTags.MY_PLANTS).performClick()
        composeRule.onNodeWithTag(HomeTags.BROWSE_MIXES).performClick()
        composeRule.onNodeWithTag(HomeTags.ABOUT).performClick()
        assertThat(listOf(identify, myPlants, browse, about)).containsExactly(true, true, true, true)
    }

    @Test
    fun recentCarouselRendersAndRowClickFires() {
        val row =
            MyPlantRow(
                speciesId = "ficus-elastica",
                displayName = "Rubber plant",
                source = IdSource.ON_DEVICE_MODEL,
                confidencePct = 70,
                savedAtEpochMs = 1L,
            )
        var clicked: MyPlantRow? = null
        content(recent = listOf(row), onRecentClick = { clicked = it })
        composeRule.onNodeWithTag(HomeTags.RECENT_ROW).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.recentCardTag("ficus-elastica")).performClick()
        assertThat(clicked?.speciesId).isEqualTo("ficus-elastica")
    }
}
