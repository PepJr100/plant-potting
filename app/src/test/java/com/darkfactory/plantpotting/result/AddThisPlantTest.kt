package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.persistence.FakePlantLogStore
import com.darkfactory.plantpotting.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class AddThisPlantTest {
    @get:Rule val composeRule = createComposeRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm(
        store: FakePlantLogStore,
        label: String = "Chinese Money Plant (Pilea peperomioides)",
        pct: Int = 96,
    ): AddThisPlantViewModel {
        val saved =
            SavedStateHandle(
                mapOf(
                    Routes.ARG_MODEL_CLASS_LABEL to label,
                    Routes.ARG_CONFIDENCE_PCT to pct,
                ),
            )
        return AddThisPlantViewModel(savedStateHandle = saved, plantLogStore = store)
    }

    @Test
    fun addButtonIncrementsRequestCounterAndShowsConfirmation() =
        runBlocking {
            val store = FakePlantLogStore(nowEpochMs = 11L)
            val v = vm(store)
            composeRule.setContent {
                AddThisPlantScreen(viewModel = v, onPickManually = {})
            }
            composeRule.onNodeWithTag(AddThisPlantTags.CLASS_NAME).assertIsDisplayed()
            composeRule.onNodeWithText("Chinese Money Plant (Pilea peperomioides)").assertIsDisplayed()

            composeRule.onNodeWithTag(AddThisPlantTags.ADD_BUTTON).performClick()

            // Confirmation replaces the button; the store counter is incremented.
            composeRule.onNodeWithTag(AddThisPlantTags.CONFIRMATION).assertIsDisplayed()
            val req = store.snapshot().addPlantRequests.single()
            assertThat(req.modelClassLabel).isEqualTo("Chinese Money Plant (Pilea peperomioides)")
            assertThat(req.count).isEqualTo(1)
        }

    @Test
    fun pickManuallyFiresCallback() {
        val store = FakePlantLogStore()
        var picked = false
        composeRule.setContent {
            AddThisPlantScreen(viewModel = vm(store), onPickManually = { picked = true })
        }
        composeRule.onNodeWithTag(AddThisPlantTags.PICK_MANUALLY).performClick()
        assertThat(picked).isTrue()
    }

    @Test
    fun secondAddDoesNotDoubleCountViaUi() =
        runBlocking {
            // The UI flips to the confirmation state after the first tap, so the button is gone —
            // a single request is logged. (Each tap = one request is the documented semantics; the
            // screen guards against an accidental double-count by hiding the button.)
            val store = FakePlantLogStore()
            val v = vm(store)
            composeRule.setContent {
                AddThisPlantScreen(viewModel = v, onPickManually = {})
            }
            composeRule.onNodeWithTag(AddThisPlantTags.ADD_BUTTON).performClick()
            composeRule.onNodeWithTag(AddThisPlantTags.ADD_BUTTON).assertDoesNotExist()
            assertThat(
                store
                    .snapshot()
                    .addPlantRequests
                    .single()
                    .count,
            ).isEqualTo(1)
        }
}
