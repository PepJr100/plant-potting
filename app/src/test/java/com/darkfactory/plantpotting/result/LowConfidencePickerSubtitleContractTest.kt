package com.darkfactory.plantpotting.result

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
 * PLANTPOTTING-0005 §0.7 — contract-lock test. RED today (no `lowConf.subtitle`
 * node exists in [LowConfidencePickerScreen]); flips GREEN after §1.1 renders the
 * sub-headline tagged `LowConfidencePickerTags.SUBTITLE` (= `"lowConf.subtitle"`).
 *
 * Literal tag string used here so the JVM compile keeps working between Phase 0
 * (contract land) and Phase 1 (constant introduced). The literal must match the
 * §1.1 constant value verbatim.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class LowConfidencePickerSubtitleContractTest {
    @get:Rule val composeRule = createComposeRule()

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
