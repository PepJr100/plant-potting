package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

/**
 * PLANTPOTTING-0003 §6.4 — vertical layout:
 *  - up-to-3 top-mapped candidate chips (omitted when no candidates mapped)
 *  - search field
 *  - full 16-species KB list (filtered by the search)
 *  - "Pick by archetype" CTA at the bottom
 *
 * Routing decisions belong to the navhost; this screen only emits callbacks.
 */
@Composable
fun LowConfidencePickerScreen(
    viewModel: LowConfidencePickerViewModel,
    onSpeciesPicked: (String) -> Unit,
    onPickByArchetype: () -> Unit,
) {
    val query by viewModel.query.collectAsState()
    val filtered by viewModel.filteredSpecies.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "We couldn't identify your plant confidently.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag(LowConfidencePickerTags.HEADLINE),
        )
        if (viewModel.topCandidates.isNotEmpty()) {
            Text(
                text = "Did you mean…",
                style = MaterialTheme.typography.bodyMedium,
            )
            Column(modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.TOP_ROW)) {
                viewModel.topCandidates.forEach { c ->
                    AssistChip(
                        onClick = { onSpeciesPicked(c.speciesId) },
                        label = {
                            Text(
                                text = "${c.commonName.ifBlank { c.scientificName }} (${c.probabilityPct}%)",
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .testTag(LowConfidencePickerTags.candidateTag(c.speciesId)),
                    )
                }
            }
            HorizontalDivider()
        }
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search species") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.SEARCH),
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.SPECIES_LIST)) {
            items(filtered, key = { it.id }) { sp ->
                TextButton(
                    onClick = { onSpeciesPicked(sp.id) },
                    modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.speciesTag(sp.id)),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = sp.scientificName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        )
                        if (sp.commonNames.isNotEmpty()) {
                            Text(
                                text = sp.commonNames.first(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = onPickByArchetype,
            modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.PICK_BY_ARCHETYPE),
        ) {
            Text("I don't know — pick by archetype")
        }
    }
}

object LowConfidencePickerTags {
    const val HEADLINE = "lowConf.headline"
    const val TOP_ROW = "lowConf.topRow"
    const val SEARCH = "lowConf.search"
    const val SPECIES_LIST = "lowConf.speciesList"
    const val PICK_BY_ARCHETYPE = "lowConf.pickByArchetype"

    fun candidateTag(speciesId: String): String = "lowConf.candidate.$speciesId"

    fun speciesTag(speciesId: String): String = "lowConf.species.$speciesId"
}
