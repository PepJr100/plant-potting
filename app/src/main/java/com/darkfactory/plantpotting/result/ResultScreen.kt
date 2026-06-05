package com.darkfactory.plantpotting.result

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.identify.IdSource

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onSeePottingMix: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.notFound) {
            Text(
                text = stringResource(id = R.string.result_not_found),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(ResultScreenTags.NOT_FOUND),
            )
            return@Column
        }

        Text(
            text = state.scientificName,
            style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
            modifier = Modifier.testTag(ResultScreenTags.SCIENTIFIC_NAME),
        )
        if (state.commonName.isNotEmpty()) {
            Text(
                text = "(${state.commonName})",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(ResultScreenTags.COMMON_NAME),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        AssistChip(
            onClick = {},
            label = { Text(stringResource(id = badgeStringRes(state.source, state.lowConfidence))) },
            colors = AssistChipDefaults.assistChipColors(),
            modifier = Modifier.testTag(ResultScreenTags.SOURCE_BADGE),
        )
        // PLANTPOTTING-0010 D2 — numeric confidence + progress bar on the on-device high-confidence
        // path; absent (null) on stub/picker flows → render nothing.
        state.confidencePct?.let { pct ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.result_confidence_label, pct),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag(ResultScreenTags.CONFIDENCE_PCT),
            )
            LinearProgressIndicator(
                progress = pct / 100f,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(ResultScreenTags.CONFIDENCE_BAR),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSeePottingMix(state.speciesId) },
            modifier = Modifier.testTag(ResultScreenTags.SEE_POTTING_MIX),
        ) {
            Text(stringResource(id = R.string.result_see_potting_mix))
        }
    }
}

/**
 * Picks the badge string per PLANTPOTTING-0003 §5.1 / §5.6:
 *  - Stub sources (deterministic + random) → legacy "Stub identifier" copy.
 *  - On-device, high confidence → "On-device match".
 *  - On-device, low confidence → "On-device match (low confidence)".
 *  - Cloud → "Cloud match unavailable in this offline build".
 */
@StringRes
internal fun badgeStringRes(
    source: IdSource,
    lowConfidence: Boolean,
): Int =
    when (source) {
        IdSource.STUB_DETERMINISTIC, IdSource.STUB_RANDOM -> R.string.result_badge_stub
        IdSource.ON_DEVICE_MODEL ->
            if (lowConfidence) R.string.result_badge_on_device_low else R.string.result_badge_on_device
        IdSource.CLOUD -> R.string.result_badge_cloud
    }

object ResultScreenTags {
    const val SCIENTIFIC_NAME = "result.scientificName"
    const val COMMON_NAME = "result.commonName"
    const val SOURCE_BADGE = "result.sourceBadge"

    // PLANTPOTTING-0010 D2 — confidence %/bar (present only when confidencePct is non-null).
    const val CONFIDENCE_PCT = "result.confidencePct"
    const val CONFIDENCE_BAR = "result.confidenceBar"

    /**
     * Backward-compatible alias for tests / scripts that still reference the legacy
     * stub-badge tag. Same value as [SOURCE_BADGE]. PLANTPOTTING-0003 §5.6 retired the
     * stub-only badge in favour of a source-driven dispatch.
     */
    @Deprecated("Use SOURCE_BADGE — the badge is no longer stub-specific.", ReplaceWith("SOURCE_BADGE"))
    const val STUB_BADGE = SOURCE_BADGE
    const val SEE_POTTING_MIX = "result.seePottingMix"
    const val NOT_FOUND = "result.notFound"
}
