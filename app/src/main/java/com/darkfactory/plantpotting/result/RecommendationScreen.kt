package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 (review feedback / Dribbble layout) — the potting-mix page in the card layout
 * language: a rounded header card (archetype name + blend chip), the rationale, then the recipe as
 * a list of rounded rows each with a proportion "pill".
 */
@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel,
    onRetake: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val s = state) {
            RecommendationUiState.Loading -> {
                Text(
                    text = "…",
                    modifier = Modifier.testTag(RecommendationScreenTags.LOADING),
                )
            }
            RecommendationUiState.NotFound -> {
                Text(
                    text = stringResource(id = R.string.recommendation_not_found),
                    modifier = Modifier.testTag(RecommendationScreenTags.NOT_FOUND),
                )
            }
            is RecommendationUiState.Ready -> ReadyContent(s, onRetake)
        }
    }
}

@Composable
private fun ColumnScope.ReadyContent(
    s: RecommendationUiState.Ready,
    onRetake: () -> Unit,
) {
    // Hero header card.
    Card(
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = stringResource(id = R.string.recommendation_recipe_heading_for),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = s.archetypeName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(RecommendationScreenTags.ARCHETYPE_NAME),
                )
                if (s.isBlend) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(id = R.string.recommendation_blend_chip)) },
                        modifier = Modifier.testTag(RecommendationScreenTags.BLEND_CHIP),
                    )
                }
            }
        }
    }

    Text(
        text = s.rationale,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.testTag(RecommendationScreenTags.RATIONALE),
    )

    Text(
        text = stringResource(id = R.string.recommendation_recipe_heading),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
    // Plain column (recipes are short, ≤6 rows) inside the scrollable screen so every row renders.
    Column(
        modifier = Modifier.fillMaxWidth().testTag(RecommendationScreenTags.RECIPE_LIST),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        s.recipe.forEach { ingredient ->
            Card(
                shape = MaterialTheme.shapes.medium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(RecommendationScreenTags.RECIPE_ROW),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = ingredient.ingredient,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Text(
                            text = "${ingredient.proportionPct}%",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Button(
        onClick = onRetake,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(RecommendationScreenTags.RETAKE),
    ) {
        Text(stringResource(id = R.string.recommendation_retake))
    }
}

object RecommendationScreenTags {
    const val ARCHETYPE_NAME = "recommendation.archetypeName"
    const val BLEND_CHIP = "recommendation.blendChip"
    const val RATIONALE = "recommendation.rationale"
    const val RECIPE_LIST = "recommendation.recipeList"
    const val RECIPE_ROW = "recommendation.recipeRow"
    const val RETAKE = "recommendation.retake"
    const val LOADING = "recommendation.loading"
    const val NOT_FOUND = "recommendation.notFound"
}
