package com.darkfactory.plantpotting.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.ui.theme.PlantPottingTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0010 (review feedback) — the landing screen offers exactly the two destinations the
 * principal asked for and fires the right callbacks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class HomeScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun rendersBothEntryPoints() {
        composeRule.setContent {
            PlantPottingTheme { HomeScreen(onIdentify = {}, onMyPlants = {}) }
        }
        composeRule.onNodeWithTag(HomeTags.IDENTIFY).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.MY_PLANTS).assertIsDisplayed()
    }

    @Test
    fun identifyFiresCallback() {
        var identify = false
        composeRule.setContent {
            PlantPottingTheme { HomeScreen(onIdentify = { identify = true }, onMyPlants = {}) }
        }
        composeRule.onNodeWithTag(HomeTags.IDENTIFY).performClick()
        assertThat(identify).isTrue()
    }

    @Test
    fun myPlantsFiresCallback() {
        var myPlants = false
        composeRule.setContent {
            PlantPottingTheme { HomeScreen(onIdentify = {}, onMyPlants = { myPlants = true }) }
        }
        composeRule.onNodeWithTag(HomeTags.MY_PLANTS).performClick()
        assertThat(myPlants).isTrue()
    }
}
