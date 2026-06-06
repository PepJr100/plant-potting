package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.ui.HomeIconButton

/**
 * PLANTPOTTING-0010 (review feedback / Dribbble layout) — "Browse mixes": each substrate archetype
 * is a large rounded card (section-header + card-list language), tap → archetype-recommendation.
 */
@Composable
fun ArchetypePickerScreen(
    viewModel: ArchetypePickerViewModel,
    onArchetypePicked: (String) -> Unit,
    onHome: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeIconButton(onHome = onHome)
        Text(
            text = "Browse mixes",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.testTag(ArchetypePickerTags.HEADLINE),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag(ArchetypePickerTags.LIST),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(viewModel.archetypes, key = { it.id }) { archetype ->
                Card(
                    onClick = { onArchetypePicked(archetype.id) },
                    shape = MaterialTheme.shapes.large,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(ArchetypePickerTags.archetypeTag(archetype.id)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = archetype.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = archetype.shortDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

object ArchetypePickerTags {
    const val HEADLINE = "archetypePicker.headline"
    const val LIST = "archetypePicker.list"

    fun archetypeTag(archetypeId: String): String = "archetypePicker.archetype.$archetypeId"
}
