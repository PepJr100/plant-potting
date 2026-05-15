package com.darkfactory.plantpotting.result

import androidx.lifecycle.SavedStateHandle
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.recommend.Recommendation
import com.darkfactory.plantpotting.recommend.RecommendationEngine
import com.darkfactory.plantpotting.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecommendationViewModelTest {
    @Test
    fun engineSuccessProducesReadyState() {
        val engine =
            object : RecommendationEngine {
                override fun recommend(speciesId: String) =
                    Recommendation(
                        archetypeName = "Aroid Chunky",
                        recipe =
                            listOf(
                                RecipeIngredient("Pine bark", 40),
                                RecipeIngredient("Coir", 60),
                            ),
                        rationale = "Suits Monstera deliciosa.",
                        isBlend = false,
                    )

                override fun recommendByArchetype(archetypeId: String): Recommendation = error("not used")
            }
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "monstera-deliciosa"))
        val vm = RecommendationViewModel(savedStateHandle = saved, engine = engine)
        val ready = vm.state.value as RecommendationUiState.Ready
        assertThat(ready.archetypeName).isEqualTo("Aroid Chunky")
        assertThat(ready.recipe).hasSize(2)
        assertThat(ready.isBlend).isFalse()
    }

    @Test
    fun engineFailureProducesNotFound() {
        val engine =
            object : RecommendationEngine {
                override fun recommend(speciesId: String): Recommendation = throw IllegalArgumentException("unknown species: $speciesId")

                override fun recommendByArchetype(archetypeId: String): Recommendation = error("not used")
            }
        val saved = SavedStateHandle(mapOf(Routes.ARG_SPECIES_ID to "ghost"))
        val vm = RecommendationViewModel(savedStateHandle = saved, engine = engine)
        assertThat(vm.state.value).isEqualTo(RecommendationUiState.NotFound)
    }
}
