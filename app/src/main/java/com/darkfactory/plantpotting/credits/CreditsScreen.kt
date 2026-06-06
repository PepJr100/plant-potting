package com.darkfactory.plantpotting.credits

import android.content.res.AssetManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.ui.HomeButton

/**
 * PLANTPOTTING-0011 — in-app **Image credits** screen. Reference photos are CC0 / public-domain
 * (no attribution required) or **CC BY** (attribution legally required). This screen renders the
 * attribution for every CC BY image from the bundled `image_credits.tsv`, satisfying the CC BY
 * obligation. CC0/PD images are acknowledged collectively. `ImageCreditsTest` keeps the TSV in sync
 * with `docs/licenses/reference-images.md`.
 */
@Composable
fun CreditsScreen(onHome: () -> Unit) {
    val context = LocalContext.current
    val credits = remember { loadImageCredits(context.assets) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag(CreditsScreenTags.SCREEN),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.credits_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag(CreditsScreenTags.TITLE),
        )
        Text(
            text = stringResource(id = R.string.credits_intro),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        credits.forEach { c ->
            Column(modifier = Modifier.fillMaxWidth().testTag(CreditsScreenTags.rowTag(c.drawable))) {
                Text(text = c.species, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(id = R.string.credits_attribution, c.author, c.license),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(text = c.sourceUrl, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HomeButton(onHome = onHome, modifier = Modifier.fillMaxWidth())
    }
}

/** One attribution row from `image_credits.tsv`. */
data class ImageCredit(
    val drawable: String,
    val species: String,
    val author: String,
    val license: String,
    val sourceUrl: String,
)

/**
 * Parses the bundled `image_credits.tsv` (skipping `#` comments and the header row). Exposed so
 * `ImageCreditsTest` can validate the same data the screen renders.
 */
fun loadImageCredits(assets: AssetManager): List<ImageCredit> =
    assets.open("image_credits.tsv").bufferedReader().useLines { lines ->
        lines
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .drop(1) // header
            .map { it.split("\t") }
            .filter { it.size >= 5 }
            .map { ImageCredit(it[0].trim(), it[1].trim(), it[2].trim(), it[3].trim(), it[4].trim()) }
            .toList()
    }

object CreditsScreenTags {
    const val SCREEN = "credits.screen"
    const val TITLE = "credits.title"

    fun rowTag(drawable: String) = "credits.row.$drawable"
}
