package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
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
    fun rendersPlantNameArchetypeRationaleAndRecipeForSingleMapping() {
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
        val vm = makeVm(speciesId = "spathiphyllum-wallisii", commonName = "Peace lily", recommendation = rec)
        composeRule.setContent {
            RecommendationScreen(viewModel = vm, onHome = {})
        }
        // Order: plant name → picture → description → recommended mix → recipe.
        composeRule.onNodeWithText("Peace lily").assertIsDisplayed()
        composeRule.onNodeWithTag(RecommendationScreenTags.PLANT_IMAGE).assertIsDisplayed()
        composeRule.onNodeWithText("Suits Spathiphyllum wallisii: terrestrial aroid.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Moisture-Retentive").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Coco coir").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("50%").assertIsDisplayed()
        composeRule.onNodeWithText("Long-fibre sphagnum").performScrollTo().assertIsDisplayed()
        val rowCount =
            composeRule
                .onAllNodesWithTag(RecommendationScreenTags.RECIPE_ROW)
                .fetchSemanticsNodes()
                .size
        assertThat(rowCount).isEqualTo(rec.recipe.size)
    }

    @Test
    fun rendersBlendChipForBlendMapping() {
        val rec =
            Recommendation(
                archetypeName = "Aroid Chunky / Succulent Gritty blend",
                recipe = listOf(RecipeIngredient("Pine or orchid bark", 24), RecipeIngredient("Coco coir", 76)),
                rationale = "Suits Hoya carnosa: lithophytic/semi-terrestrial.",
                isBlend = true,
            )
        val vm = makeVm(speciesId = "hoya-carnosa", commonName = "Wax plant", recommendation = rec)
        composeRule.setContent {
            RecommendationScreen(viewModel = vm, onHome = {})
        }
        composeRule.onNodeWithTag(RecommendationScreenTags.BLEND_CHIP).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun homeButtonEmitsCallback() {
        val rec =
            Recommendation(
                archetypeName = "Standard Houseplant",
                recipe = listOf(RecipeIngredient("Coir", 100)),
                rationale = "Suits Ficus lyrata.",
                isBlend = false,
            )
        val vm = makeVm("ficus-lyrata", "Fiddle-leaf fig", rec)
        var home = false
        composeRule.setContent {
            RecommendationScreen(viewModel = vm, onHome = { home = true })
        }
        composeRule.onNodeWithTag(RecommendationScreenTags.HOME_BUTTON).performScrollTo().performClick()
        assertThat(home).isTrue()
    }

    private fun makeVm(
        speciesId: String,
        commonName: String,
        recommendation: Recommendation,
    ): RecommendationViewModel {
        val engine =
            object : RecommendationEngine {
                override fun recommend(speciesId: String): Recommendation = recommendation

                override fun recommendByArchetype(archetypeId: String): Recommendation = recommendation
            }
        val species =
            Species(
                id = speciesId,
                scientificName = "Test scientific",
                commonNames = listOf(commonName),
                aliases = emptyList(),
                mapping = ArchetypeMapping.Single("standard"),
                speciesRationale = "x",
                citations = listOf("Brief"),
            )
        val kb =
            KnowledgeBase(
                archetypes = emptyMap(),
                species = listOf(species),
                speciesIndex = mapOf(speciesId to species),
            )
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to speciesId))
        return RecommendationViewModel(savedStateHandle = saved, engine = engine, kb = kb)
    }
}
