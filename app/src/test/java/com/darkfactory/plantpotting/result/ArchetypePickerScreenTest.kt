package com.darkfactory.plantpotting.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0003 §6.5 — picker lists 8 archetypes; tap fires `onArchetypePicked(id)`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ArchetypePickerScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private val kb =
        KnowledgeBase(
            archetypes =
                mapOf(
                    "standard-houseplant" to
                        archetype("standard-houseplant", "Standard Houseplant", "Balanced mix."),
                    "aroid-chunky" to
                        archetype("aroid-chunky", "Aroid Chunky", "Bark-led mix."),
                    "succulent-gritty" to
                        archetype("succulent-gritty", "Succulent Gritty", "Mineral-led mix."),
                ),
            species = emptyList(),
            speciesIndex = emptyMap(),
        )

    @Test
    fun rendersHeadlineAndArchetypeList() {
        val vm = ArchetypePickerViewModel(kb = kb)
        composeRule.setContent {
            ArchetypePickerScreen(viewModel = vm, onArchetypePicked = {})
        }
        composeRule.onNodeWithTag(ArchetypePickerTags.HEADLINE).assertIsDisplayed()
        composeRule.onNodeWithTag(ArchetypePickerTags.LIST).assertIsDisplayed()
    }

    @Test
    fun tappingAnArchetypeFiresCallbackWithItsId() {
        val vm = ArchetypePickerViewModel(kb = kb)
        var picked: String? = null
        composeRule.setContent {
            ArchetypePickerScreen(viewModel = vm, onArchetypePicked = { picked = it })
        }
        composeRule
            .onNodeWithTag(ArchetypePickerTags.archetypeTag("aroid-chunky"))
            .performClick()
        assertThat(picked).isEqualTo("aroid-chunky")
    }

    private fun archetype(
        id: String,
        displayName: String,
        shortDescription: String,
    ) = Archetype(
        id = id,
        displayName = displayName,
        shortDescription = shortDescription,
        recipe = listOf(RecipeIngredient("coir", 100)),
        rationaleTemplate = "Suits {species}.",
        citations = listOf("Brief"),
    )
}
