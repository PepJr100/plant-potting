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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R

@OptIn(ExperimentalComposeUiApi::class)
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
                .padding(24.dp)
                // Surface Compose testTags as `resource-id` in the
                // AccessibilityNodeInfo tree so `adb shell uiautomator dump`
                // (used by `scripts/integration-flow.ps1`) can locate the
                // archetype name and count `recommendation.recipeRow` nodes.
                .semantics { testTagsAsResourceId = true },
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = s.archetypeName,
            style = MaterialTheme.typography.headlineMedium,
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
    Text(
        text = s.rationale,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.testTag(RecommendationScreenTags.RATIONALE),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(id = R.string.recommendation_recipe_heading),
        style = MaterialTheme.typography.titleMedium,
    )
    LazyColumn(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(RecommendationScreenTags.RECIPE_LIST),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(s.recipe) { ingredient ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag(RecommendationScreenTags.RECIPE_ROW),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ingredient.ingredient,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${ingredient.proportionPct}%",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
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
