package com.darkfactory.plantpotting.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * PLANTPOTTING-0010 D5 — debug-only in-app theme switcher. Lists the [ThemeCandidate]s and persists
 * the selection (via [ThemeSwitcherViewModel] → DataStore); the app re-applies it live. Reached only
 * from a `BuildConfig.DEBUG`-gated affordance, so it never ships in release.
 */
@Composable
fun DebugThemeSwitcherScreen(viewModel: ThemeSwitcherViewModel) {
    val selected by viewModel.selected.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag(ThemeSwitcherTags.SCREEN),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Theme (debug)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Pick a candidate — the app re-themes immediately. Production default is LEAF.",
            style = MaterialTheme.typography.bodyMedium,
        )
        ThemeCandidate.entries.forEach { candidate ->
            val isSelected = candidate == selected
            if (isSelected) {
                Button(
                    onClick = { viewModel.select(candidate) },
                    colors = ButtonDefaults.buttonColors(),
                    modifier = Modifier.fillMaxWidth().testTag(ThemeSwitcherTags.option(candidate)),
                ) {
                    Text("${candidate.name} ✓")
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.select(candidate) },
                    modifier = Modifier.fillMaxWidth().testTag(ThemeSwitcherTags.option(candidate)),
                ) {
                    Text(candidate.name)
                }
            }
        }
    }
}

object ThemeSwitcherTags {
    const val SCREEN = "themeSwitcher.screen"

    fun option(candidate: ThemeCandidate): String = "themeSwitcher.option.${candidate.name}"
}
