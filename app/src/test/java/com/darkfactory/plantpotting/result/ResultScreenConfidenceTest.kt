package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.darkfactory.plantpotting.ui.navigation.Routes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0010 D2 — confirms `ResultScreen` renders the numeric confidence + progress bar on
 * the on-device high-confidence path and degrades to neither when confidence is absent (stub flows).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ResultScreenConfidenceTest {
    @get:Rule val composeRule = createComposeRule()

    private val kb = fixtureKb()

    private fun vm(
        confidencePct: Int?,
        source: IdSource = IdSource.ON_DEVICE_MODEL,
    ): ResultViewModel {
        val args =
            mutableMapOf<String, Any?>(
                Routes.ARG_SPECIES_ID to "ficus-lyrata",
                Routes.ARG_SOURCE to source.name,
                Routes.ARG_CONFIDENCE_PCT to (confidencePct ?: Routes.CONFIDENCE_ABSENT),
            )
        return ResultViewModel(
            savedStateHandle = SavedStateHandle(args),
            kb = kb,
            plantLogStore = com.darkfactory.plantpotting.persistence.FakePlantLogStore(),
        )
    }

    @Test
    fun confidencePresentRendersPercentAndBar() {
        composeRule.setContent {
            ResultScreen(viewModel = vm(confidencePct = 92), onSeePottingMix = {})
        }
        // ResultScreen is scrollable (hero image pushes content down); scroll the bar (bottom-most
        // of the %/bar pair) into view, which brings the adjacent % with it.
        composeRule.onNodeWithTag(ResultScreenTags.CONFIDENCE_BAR).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(ResultScreenTags.CONFIDENCE_PCT).assertIsDisplayed()
        composeRule.onNodeWithText("Confidence: 92%").assertIsDisplayed()
    }

    @Test
    fun confidenceAbsentRendersNeitherPercentNorBar() {
        composeRule.setContent {
            ResultScreen(viewModel = vm(confidencePct = null, source = IdSource.STUB_DETERMINISTIC), onSeePottingMix = {})
        }
        composeRule.onNodeWithTag(ResultScreenTags.CONFIDENCE_PCT).assertDoesNotExist()
        composeRule.onNodeWithTag(ResultScreenTags.CONFIDENCE_BAR).assertDoesNotExist()
    }

    @Test
    fun referenceImageRendersForSpeciesWithoutBundledPhoto() {
        // PLANTPOTTING-0010 Phase 6 — missing-image metadata must not crash rendering: the placeholder
        // shows for a species with no bundled CC0/PD photo (ficus-lyrata).
        composeRule.setContent {
            ResultScreen(viewModel = vm(confidencePct = 50), onSeePottingMix = {})
        }
        composeRule.onNodeWithTag(ResultScreenTags.REFERENCE_IMAGE).assertIsDisplayed()
    }

    private fun fixtureKb(): KnowledgeBase {
        val arch =
            Archetype(
                id = "standard",
                displayName = "Standard",
                shortDescription = "",
                recipe = listOf(RecipeIngredient("coir", 100)),
                rationaleTemplate = "Suits {species}.",
                citations = listOf("Brief"),
            )
        val list =
            listOf(
                Species(
                    id = "ficus-lyrata",
                    scientificName = "Ficus lyrata",
                    commonNames = listOf("Fiddle-leaf fig"),
                    aliases = emptyList(),
                    mapping = ArchetypeMapping.Single("standard"),
                    speciesRationale = "Ficus lyrata is a fig.",
                    citations = listOf("Brief"),
                ),
            )
        return KnowledgeBase(
            archetypes = mapOf("standard" to arch),
            species = list,
            speciesIndex = list.associateBy { it.id },
        )
    }
}
