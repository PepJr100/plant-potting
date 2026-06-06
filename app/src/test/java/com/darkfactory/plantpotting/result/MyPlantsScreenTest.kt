package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.persistence.FakePlantLogStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MyPlantsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyStoreShowsEmptyState() {
        val vm = MyPlantsViewModel(FakePlantLogStore())
        composeRule.setContent {
            MyPlantsScreen(viewModel = vm, onPlantClick = {})
        }
        composeRule.onNodeWithTag(MyPlantsTags.EMPTY).assertIsDisplayed()
        composeRule.onNodeWithTag(MyPlantsTags.LIST).assertDoesNotExist()
    }

    @Test
    fun savedPlantsRenderAsRows() {
        val store = FakePlantLogStore(nowEpochMs = 1_000L)
        runBlocking {
            store.saveIdentifiedPlant("ficus-elastica", "Rubber plant", IdSource.ON_DEVICE_MODEL.name, 70)
        }
        val vm = MyPlantsViewModel(store)
        composeRule.setContent {
            MyPlantsScreen(viewModel = vm, onPlantClick = {})
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag(MyPlantsTags.rowTag("ficus-elastica")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(MyPlantsTags.LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Rubber plant").assertIsDisplayed()
    }

    @Test
    fun tappingRemoveDeletesTheRow() {
        val store = FakePlantLogStore(nowEpochMs = 1_000L)
        runBlocking {
            store.saveIdentifiedPlant("aloe-vera", "Aloe", IdSource.ON_DEVICE_MODEL.name, 30)
            store.saveIdentifiedPlant("ficus-elastica", "Rubber plant", IdSource.ON_DEVICE_MODEL.name, 40)
        }
        val vm = MyPlantsViewModel(store)
        composeRule.setContent {
            MyPlantsScreen(viewModel = vm, onPlantClick = {})
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag(MyPlantsTags.rowTag("aloe-vera")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(MyPlantsTags.removeTag("aloe-vera")).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag(MyPlantsTags.rowTag("aloe-vera")).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(MyPlantsTags.rowTag("ficus-elastica")).assertIsDisplayed()
    }

    @Test
    fun tappingARowEmitsThatPlant() {
        val store = FakePlantLogStore(nowEpochMs = 1_000L)
        runBlocking {
            store.saveIdentifiedPlant("aloe-vera", "Aloe", IdSource.ON_DEVICE_MODEL.name, null)
        }
        val vm = MyPlantsViewModel(store)
        var clicked: MyPlantRow? = null
        composeRule.setContent {
            MyPlantsScreen(viewModel = vm, onPlantClick = { clicked = it })
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag(MyPlantsTags.rowTag("aloe-vera")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(MyPlantsTags.rowTag("aloe-vera")).performClick()
        assertThat(clicked?.speciesId).isEqualTo("aloe-vera")
    }
}
