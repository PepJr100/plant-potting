package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * PLANTPOTTING-0003 §6.6 — flat list of 8 archetypes, minimal styling per the §6.3 de-scope
 * order. Tap → archetype-recommendation route via [onArchetypePicked].
 */
@Composable
fun ArchetypePickerScreen(
    viewModel: ArchetypePickerViewModel,
    onArchetypePicked: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Pick a substrate archetype",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag(ArchetypePickerTags.HEADLINE),
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().testTag(ArchetypePickerTags.LIST)) {
            items(viewModel.archetypes, key = { it.id }) { archetype ->
                TextButton(
                    onClick = { onArchetypePicked(archetype.id) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(ArchetypePickerTags.archetypeTag(archetype.id)),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = archetype.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = archetype.shortDescription,
                            style = MaterialTheme.typography.bodySmall,
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
