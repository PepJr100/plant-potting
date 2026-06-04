package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0005 §2.1 — the post-shutter screen for unmapped / weak / uncertain
 * predictions. AIY V1/3 maps only 2 of 16 KB species verbatim so this screen is the
 * primary post-shutter destination, not a fallback. PLANTPOTTING-0003 §6.4 layout
 * (sub-headline + up-to-3 mapped candidate chips + search + 16-species list +
 * archetype CTA) preserved; PLANTPOTTING-0005 adds the SUBTITLE row, the
 * NO_CANDIDATES_EMPTY info card, the SEARCH_EMPTY empty-state, the outlined-style
 * archetype CTA, and a trailing chevron on candidate chips.
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
    val searchActive = query.isNotBlank()
    val searchEmpty = searchActive && filtered.isEmpty()

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
        Text(
            text = stringResource(id = R.string.low_conf_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag(LowConfidencePickerTags.SUBTITLE),
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
                            val name = c.commonName.ifBlank { c.scientificName }
                            Text(
                                // PLANTPOTTING-0006 §5 — a degenerate/black capture can floor a tiny
                                // score to 0, producing "Jade plant (0%)" which reads as broken. Drop
                                // the (x%) suffix only when it floors to zero; the percentage was never
                                // the point of these chips — selection is. Suffix kept for all >= 1%.
                                text = if (c.probabilityPct > 0) "$name (${c.probabilityPct}%)" else name,
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "›",
                                style = MaterialTheme.typography.titleMedium,
                                modifier =
                                    Modifier.testTag(
                                        LowConfidencePickerTags.candidateChevronTag(c.speciesId),
                                    ),
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
        } else {
            OutlinedCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(LowConfidencePickerTags.NO_CANDIDATES_EMPTY),
            ) {
                Text(
                    text = stringResource(id = R.string.low_conf_no_candidates),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search species") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.SEARCH),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (searchEmpty) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag(LowConfidencePickerTags.SEARCH_EMPTY),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.low_conf_search_empty, query),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
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
            }
        }
        OutlinedButton(
            onClick = onPickByArchetype,
            modifier = Modifier.fillMaxWidth().testTag(LowConfidencePickerTags.PICK_BY_ARCHETYPE),
        ) {
            Text(stringResource(id = R.string.low_conf_pick_other))
        }
    }
}

object LowConfidencePickerTags {
    const val HEADLINE = "lowConf.headline"
    const val SUBTITLE = "lowConf.subtitle"
    const val TOP_ROW = "lowConf.topRow"
    const val NO_CANDIDATES_EMPTY = "lowConf.noCandidatesEmpty"
    const val SEARCH = "lowConf.search"
    const val SEARCH_EMPTY = "lowConf.searchEmpty"
    const val SPECIES_LIST = "lowConf.speciesList"
    const val PICK_BY_ARCHETYPE = "lowConf.pickByArchetype"

    fun candidateTag(speciesId: String): String = "lowConf.candidate.$speciesId"

    fun candidateChevronTag(speciesId: String): String = "lowConf.candidate.$speciesId.chevron"

    fun speciesTag(speciesId: String): String = "lowConf.species.$speciesId"
}
