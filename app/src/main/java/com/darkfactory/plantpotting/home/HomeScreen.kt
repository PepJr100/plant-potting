package com.darkfactory.plantpotting.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 (review feedback) — the app's landing screen. Two primary destinations:
 * **Identify new plant** (→ camera, via the permission gate) and **My Plants** (the saved collection).
 * Routing decisions belong to the navhost; this screen only emits callbacks.
 */
@Composable
fun HomeScreen(
    onIdentify: () -> Unit,
    onMyPlants: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_plant_placeholder),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.testTag(HomeTags.TITLE),
        )
        Text(
            text = stringResource(id = R.string.home_tagline),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onIdentify,
            modifier = Modifier.fillMaxWidth().testTag(HomeTags.IDENTIFY),
        ) {
            Text(stringResource(id = R.string.home_identify))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onMyPlants,
            modifier = Modifier.fillMaxWidth().testTag(HomeTags.MY_PLANTS),
        ) {
            Text(stringResource(id = R.string.home_my_plants))
        }
    }
}

object HomeTags {
    const val TITLE = "home.title"
    const val IDENTIFY = "home.identify"
    const val MY_PLANTS = "home.myPlants"
}
