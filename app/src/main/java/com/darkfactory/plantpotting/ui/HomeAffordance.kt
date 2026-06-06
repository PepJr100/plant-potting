package com.darkfactory.plantpotting.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 (review feedback) — the consistent "Home" affordance: a full-width filled button
 * (the potting-mix page's style) placed on every top-level screen so the user can always return to
 * the landing screen. Callers apply `Modifier.fillMaxWidth()`. Routing belongs to the navhost.
 */
@Composable
fun HomeButton(
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = HomeAffordanceTags.HOME,
) {
    Button(onClick = onHome, modifier = modifier.testTag(tag)) {
        Icon(imageVector = Icons.Filled.Home, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(id = R.string.action_home))
    }
}

object HomeAffordanceTags {
    const val HOME = "action.home"
}
