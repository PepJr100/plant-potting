package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PLANTPOTTING-0010 Phase 4 — the **My Plants** folder. Lists user-saved plants (most-recent first)
 * with a source badge, confidence (if present) and saved time; an empty state when nothing is saved.
 * Tapping a row routes to that plant via [onPlantClick]. Routing decisions belong to the navhost.
 */
@Composable
fun MyPlantsScreen(
    viewModel: MyPlantsViewModel,
    onPlantClick: (MyPlantRow) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.my_plants_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(MyPlantsTags.TITLE),
        )

        if (state.isEmpty) {
            OutlinedCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(MyPlantsTags.EMPTY),
            ) {
                Text(
                    text = stringResource(id = R.string.my_plants_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag(MyPlantsTags.LIST),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.rows, key = { it.speciesId + it.savedAtEpochMs }) { row ->
                MyPlantRowCard(row = row, onClick = { onPlantClick(row) })
            }
        }
    }
}

@Composable
private fun MyPlantRowCard(
    row: MyPlantRow,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(MyPlantsTags.rowTag(row.speciesId)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = row.displayName.ifBlank { row.speciesId },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(id = badgeStringRes(row.source, lowConfidence = false))) },
                )
                row.confidencePct?.let { pct ->
                    Text(
                        text = stringResource(id = R.string.result_confidence_label, pct),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.my_plants_saved_at, formatSavedAt(row.savedAtEpochMs)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatSavedAt(epochMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMs))

object MyPlantsTags {
    const val TITLE = "myPlants.title"
    const val EMPTY = "myPlants.empty"
    const val LIST = "myPlants.list"

    fun rowTag(speciesId: String): String = "myPlants.row.$speciesId"
}
