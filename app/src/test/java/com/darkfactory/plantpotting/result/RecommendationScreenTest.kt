package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.recommend.Recommendation
import com.darkfactory.plantpotting.recommend.RecommendationEngine
import com.darkfactory.plantpotting.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RecommendationScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun rendersArchetypeNameRationaleAndRecipeForSingleMapping() {
        val rec =
            Recommendation(
                archetypeName = "Moisture-Retentive",
                recipe =
                    listOf(
                        RecipeIngredient("Coco coir", 50),
                        RecipeIngredient("Fine bark", 20),
                        RecipeIngredient("Perlite", 20),
                        RecipeIngredient("Long-fibre sphagnum", 10),
                    ),
                rationale = "Suits Spathiphyllum wallisii: terrestrial aroid.",
                isBlend = false,
            )
        val vm = makeVm(speciesId = "spathiphyllum-wallisii", recommendation = rec)
        composeRule.setContent {
            RecommendationScreen(viewModel = vm, onRetake = {})
        }
        composeRule.onNodeWithTag(RecommendationScreenTags.ARCHETYPE_NAME).assertIsDisplayed()
        composeRule.onNodeWithText("Moisture-Retentive").assertIsDisplayed()
        composeRule
            .onNodeWithText("Suits Spathiphyllum wallisii: terrestrial aroid.")
            .assertIsDisplayed()
        // Recipe rows: ingredient and integer percent both visible.
        composeRule.onNodeWithText("Coco coir").assertIsDisplayed()
        composeRule.onNodeWithText("50%").assertIsDisplayed()
        composeRule.onNodeWithText("Long-fibre sphagnum").assertIsDisplayed()
    }

    @Test
    fun rendersBlendChipForBlendMapping() {
        val rec =
            Recommendation(
                archetypeName = "Aroid Chunky / Succulent Gritty blend",
                recipe =
                    listOf(
                        RecipeIngredient("Pine or orchid bark", 24),
                        RecipeIngredient("Coco coir", 15),
                        RecipeIngredient("Perlite or pumice", 12),
                        RecipeIngredient("Pumice or akadama", 16),
                    ),
                rationale = "Suits Hoya carnosa: lithophytic/semi-terrestrial.",
                isBlend = true,
            )
        val vm = makeVm(speciesId = "hoya-carnosa", recommendation = rec)
        composeRule.setContent {
            RecommendationScreen(viewModel = vm, onRetake = {})
        }
        composeRule.onNodeWithTag(RecommendationScreenTags.BLEND_CHIP).assertIsDisplayed()
    }

    @Test
    fun retakeButtonEmitsCallback() {
        val rec =
            Recommendation(
                archetypeName = "Standard Houseplant",
                recipe = listOf(RecipeIngredient("Coir", 100)),
                rationale = "Suits Ficus lyrata.",
                isBlend = false,
            )
        val vm = makeVm("ficus-lyrata", rec)
        var retook = false
        composeRule.setContent {
            RecommendationScreen(viewModel = vm, onRetake = { retook = true })
        }
        composeRule.onNodeWithTag(RecommendationScreenTags.RETAKE).performClick()
        assertThat(retook).isTrue()
    }

    private fun makeVm(
        speciesId: String,
        recommendation: Recommendation,
    ): RecommendationViewModel {
        val engine =
            object : RecommendationEngine {
                override fun recommend(speciesId: String): Recommendation = recommendation
            }
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to speciesId))
        return RecommendationViewModel(savedStateHandle = saved, engine = engine)
    }
}
