package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.SavedStateHandle
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
 * PLANTPOTTING-0005 §0.7 — contract-lock test. Originally RED (no `lowConf.subtitle`
 * node existed in [LowConfidencePickerScreen]); flipped GREEN by §1.1 which rendered the
 * sub-headline tagged `LowConfidencePickerTags.SUBTITLE` (= `"lowConf.subtitle"`).
 *
 * PLANTPOTTING-0006 §G4 — strengthened from a presence-only lock to a *copy* lock. The
 * 0005 review flagged the original copy ("This model recognises a limited plant
 * vocabulary…") as engineer-speak leaking into a product surface. This test now pins the
 * de-jargoned copy verbatim and asserts the word "model" is gone, so a regression to the
 * old wording reds the suite.
 *
 * Literal tag string used here so the JVM compile keeps working independent of the
 * `LowConfidencePickerTags.SUBTITLE` constant; the literal must match it verbatim.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class LowConfidencePickerSubtitleContractTest {
    @get:Rule val composeRule = createComposeRule()

    private val expectedCopy = "We're best at common houseplants — confirm or pick from the list below."

    @Test
    fun subtitleIsPresentBelowHeadline() {
        val vm =
            LowConfidencePickerViewModel(
                savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_CANDIDATES to "")),
                kb = fixtureKb(),
            )
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = vm, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        composeRule.onNodeWithTag("lowConf.subtitle").assertExists()
    }

    @Test
    fun subtitleReadsTheDeJargonedCopyAndDropsModel() {
        val vm =
            LowConfidencePickerViewModel(
                savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_CANDIDATES to "")),
                kb = fixtureKb(),
            )
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = vm, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        // Pins the exact de-jargoned copy and proves the engineer-speak "model" is gone.
        composeRule.onNodeWithTag("lowConf.subtitle").assertTextEquals(expectedCopy)
        org.junit.Assert.assertFalse(
            "Subtitle copy must not contain the word \"model\"",
            expectedCopy.lowercase().contains("model"),
        )
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
                    id = "monstera-deliciosa",
                    scientificName = "Monstera deliciosa",
                    commonNames = listOf("Swiss cheese plant"),
                    aliases = emptyList(),
                    mapping = ArchetypeMapping.Single("standard"),
                    speciesRationale = "Monstera deliciosa.",
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
