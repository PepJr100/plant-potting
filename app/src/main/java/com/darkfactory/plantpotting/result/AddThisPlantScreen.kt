package com.darkfactory.plantpotting.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 Pillar B — the "Add this plant" wireframe. Shown when the model is *strongly*
 * confident about a class with **no KB entry**. Offers an "Add this plant" button (logs a local
 * request only — it does NOT write a KB species row) and a "Pick manually" secondary that falls
 * through to the existing low-confidence picker.
 */
@Composable
fun AddThisPlantScreen(
    viewModel: AddThisPlantViewModel,
    onPickManually: () -> Unit,
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
            text = stringResource(id = R.string.add_plant_headline),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag(AddThisPlantTags.HEADLINE),
        )
        Text(
            text = state.modelClassLabel,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag(AddThisPlantTags.CLASS_NAME),
        )
        Text(
            text = stringResource(id = R.string.result_confidence_label, state.confidencePct),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag(AddThisPlantTags.CONFIDENCE),
        )
        Text(
            text = stringResource(id = R.string.add_plant_explainer),
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.requested) {
            Text(
                text = stringResource(id = R.string.add_plant_confirmation),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(AddThisPlantTags.CONFIRMATION),
            )
        } else {
            Button(
                onClick = { viewModel.onAddThisPlant() },
                modifier = Modifier.fillMaxWidth().testTag(AddThisPlantTags.ADD_BUTTON),
            ) {
                Text(stringResource(id = R.string.add_plant_add_button))
            }
        }

        OutlinedButton(
            onClick = onPickManually,
            modifier = Modifier.fillMaxWidth().testTag(AddThisPlantTags.PICK_MANUALLY),
        ) {
            Text(stringResource(id = R.string.add_plant_pick_manually))
        }
    }
}

object AddThisPlantTags {
    const val HEADLINE = "addPlant.headline"
    const val CLASS_NAME = "addPlant.className"
    const val CONFIDENCE = "addPlant.confidence"
    const val ADD_BUTTON = "addPlant.addButton"
    const val CONFIRMATION = "addPlant.confirmation"
    const val PICK_MANUALLY = "addPlant.pickManually"
}
