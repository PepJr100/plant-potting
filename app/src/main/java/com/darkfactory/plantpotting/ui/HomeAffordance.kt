package com.darkfactory.plantpotting.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 (review feedback) — a consistent "Home" affordance placed on the top-level
 * screens (camera, browse mixes, my plants, result, add-this-plant, picker) so the user can always
 * return to the landing screen. Routing belongs to the navhost; this only emits [onHome].
 */
@Composable
fun HomeIconButton(
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    IconButton(onClick = onHome, modifier = modifier.testTag(HomeAffordanceTags.HOME)) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = stringResource(id = R.string.action_home),
            tint = tint,
        )
    }
}

object HomeAffordanceTags {
    const val HOME = "action.home"
}
