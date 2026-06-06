package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.darkfactory.plantpotting.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0003 §6.3 — Compose-UI assertions for the picker:
 *  - top-3 candidates render with `(N%)` suffix when present
 *  - 16-species manual list renders
 *  - tapping a row emits `onSpeciesPicked(...)` with the species id
 *  - tapping "Pick by archetype" emits `onPickByArchetype()`
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class LowConfidencePickerScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private val kb = fixtureKb()

    private fun vm(candidatesArg: String?): LowConfidencePickerViewModel {
        val saved = SavedStateHandle(mapOf(Routes.ARG_CANDIDATES to candidatesArg.orEmpty()))
        return LowConfidencePickerViewModel(savedStateHandle = saved, kb = kb)
    }

    @Test
    fun rendersTopThreeChipsWhenCandidatesPresent() {
        val v = vm("monstera-deliciosa|72,ficus-lyrata|18")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        composeRule.onNodeWithTag(LowConfidencePickerTags.TOP_ROW).assertIsDisplayed()
        composeRule.onNodeWithText("Swiss cheese plant (72%)").assertIsDisplayed()
        composeRule.onNodeWithText("Fiddle-leaf fig (18%)").assertIsDisplayed()
    }

    @Test
    fun eachCandidateOptionRendersAReferenceThumbnail() {
        // PLANTPOTTING-0011 — the three suggestions carry a small reference-image thumbnail.
        val v = vm("monstera-deliciosa|72,ficus-lyrata|18")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        // The clickable card merges child semantics, so query the thumbnail in the unmerged tree.
        composeRule.onNodeWithTag(LowConfidencePickerTags.candidateImageTag("monstera-deliciosa"), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag(LowConfidencePickerTags.candidateImageTag("ficus-lyrata"), useUnmergedTree = true).assertExists()
    }

    // -- PLANTPOTTING-0010 A4 — search containment -----------------------------

    @Test
    fun speciesListIsCollapsedByDefaultShowingThePrompt() {
        val v = vm("")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        // Contained: the full list is NOT always-visible; a tap-prompt stands in for it.
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH_PROMPT).assertIsDisplayed()
        composeRule.onNodeWithTag(LowConfidencePickerTags.SPECIES_LIST).assertDoesNotExist()
    }

    @Test
    fun typingRevealsSpeciesListContainedWithinSearch() {
        val v = vm("")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).performTextInput("Monstera")
        composeRule.onNodeWithTag(LowConfidencePickerTags.SPECIES_LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH_PROMPT).assertDoesNotExist()
        composeRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
    }

    @Test
    fun focusingSearchRevealsTheFullSpeciesList() {
        val v = vm("")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        // Engaging the search (focus via click) reveals the full list with a blank query.
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).performClick()
        composeRule.onNodeWithTag(LowConfidencePickerTags.SPECIES_LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
    }

    @Test
    fun tappingATopRowEmitsCandidateSpeciesId() {
        val v = vm("monstera-deliciosa|72,ficus-lyrata|18")
        var picked: String? = null
        composeRule.setContent {
            LowConfidencePickerScreen(
                viewModel = v,
                onSpeciesPicked = { picked = it },
                onPickByArchetype = {},
            )
        }
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.candidateTag("monstera-deliciosa"))
            .performClick()
        assertThat(picked).isEqualTo("monstera-deliciosa")
    }

    @Test
    fun tappingPickByArchetypeFiresCallback() {
        val v = vm("")
        var fired = false
        composeRule.setContent {
            LowConfidencePickerScreen(
                viewModel = v,
                onSpeciesPicked = {},
                onPickByArchetype = { fired = true },
            )
        }
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.PICK_BY_ARCHETYPE)
            .performClick()
        assertThat(fired).isTrue()
    }

    @Test
    fun tappingASpeciesRowEmitsThatSpeciesId() {
        val v = vm("")
        var picked: String? = null
        composeRule.setContent {
            LowConfidencePickerScreen(
                viewModel = v,
                onSpeciesPicked = { picked = it },
                onPickByArchetype = {},
            )
        }
        // Reveal the (contained) list by searching, then tap the filtered row.
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).performTextInput("Ficus")
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.speciesTag("ficus-lyrata"))
            .performClick()
        assertThat(picked).isEqualTo("ficus-lyrata")
    }

    // -- PLANTPOTTING-0006 §5 — (0%) chip suffix suppression -------------------

    @Test
    fun zeroPercentCandidateRendersNameWithoutSuffixAndStaysSelectable() {
        val v = vm("ficus-lyrata|0")
        var picked: String? = null
        composeRule.setContent {
            LowConfidencePickerScreen(
                viewModel = v,
                onSpeciesPicked = { picked = it },
                onPickByArchetype = {},
            )
        }
        // No chip anywhere renders the degenerate "(0%)" suffix.
        composeRule.onNodeWithText("(0%)", substring = true).assertDoesNotExist()
        // The candidate chip is still present, shows the name, and remains pickable —
        // dropping the suffix is presentational only; this is a picker.
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.candidateTag("ficus-lyrata"))
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertTextContains("Fiddle-leaf fig", substring = true)
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.candidateTag("ficus-lyrata"))
            .performClick()
        assertThat(picked).isEqualTo("ficus-lyrata")
    }

    @Test
    fun nonZeroPercentCandidateKeepsItsSuffix() {
        val v = vm("monstera-deliciosa|72")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        // A normal candidate is unchanged — the (x%) suffix is preserved for >= 1%.
        composeRule.onNodeWithText("Swiss cheese plant (72%)").assertIsDisplayed()
    }

    // -- §1.2 / §1.3 — empty-candidate info card -------------------------------

    @Test
    fun emptyCandidatesShowsInfoCard() {
        val v = vm("")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        composeRule.onNodeWithTag(LowConfidencePickerTags.NO_CANDIDATES_EMPTY).assertIsDisplayed()
        // Negative: the chip row is gone when there are no top candidates.
        composeRule.onNodeWithTag(LowConfidencePickerTags.TOP_ROW).assertDoesNotExist()
    }

    @Test
    fun nonEmptyCandidatesHidesInfoCard() {
        val v = vm("monstera-deliciosa|72")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        composeRule.onNodeWithTag(LowConfidencePickerTags.TOP_ROW).assertIsDisplayed()
        composeRule.onNodeWithTag(LowConfidencePickerTags.NO_CANDIDATES_EMPTY).assertDoesNotExist()
    }

    // -- §1.4 / §1.5 — search empty-state --------------------------------------

    @Test
    fun searchWithNoMatchesShowsEmptyState() {
        val v = vm("")
        composeRule.setContent {
            LowConfidencePickerScreen(viewModel = v, onSpeciesPicked = {}, onPickByArchetype = {})
        }
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).performTextInput("zzzz")
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH_EMPTY).assertIsDisplayed()
        composeRule.onNodeWithTag(LowConfidencePickerTags.SPECIES_LIST).assertDoesNotExist()
    }

    // -- §1.8 — small-screen reachability --------------------------------------

    @Test
    fun lowConfidencePickerSmallScreenReachability() {
        val v = vm("")
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(width = 360.dp, height = 640.dp)) {
                LowConfidencePickerScreen(
                    viewModel = v,
                    onSpeciesPicked = {},
                    onPickByArchetype = {},
                )
            }
        }
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).assertIsDisplayed()
        // The contained list is reachable on a small screen once search is engaged.
        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).performTextInput("Monstera")
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.speciesTag("monstera-deliciosa"))
            .assertIsDisplayed()
    }

    // ---- fixtures ----

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
                species("monstera-deliciosa", "Monstera deliciosa", "Swiss cheese plant"),
                species("ficus-lyrata", "Ficus lyrata", "Fiddle-leaf fig"),
                species("dracaena-trifasciata", "Dracaena trifasciata", "Snake plant"),
            )
        return KnowledgeBase(
            archetypes = mapOf("standard" to arch),
            species = list,
            speciesIndex = list.associateBy { it.id },
        )
    }

    private fun species(
        id: String,
        scientificName: String,
        common: String,
    ) = Species(
        id = id,
        scientificName = scientificName,
        commonNames = listOf(common),
        aliases = emptyList(),
        mapping = ArchetypeMapping.Single("standard"),
        speciesRationale = "$scientificName.",
        citations = listOf("Brief"),
    )
}
