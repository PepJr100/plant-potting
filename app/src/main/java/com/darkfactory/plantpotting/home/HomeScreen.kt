package com.darkfactory.plantpotting.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.result.MyPlantRow

/**
 * PLANTPOTTING-0010 (review feedback) — the landing screen, rebuilt in the Dribbble-concept layout
 * language the principal referenced: a friendly greeting header, a 2×2 grid of large rounded
 * "pastel" tiles (the primary destinations), and a horizontal **recent plants** carousel below.
 *
 * Tile colours use the active theme's M3 *container* roles, so the layout re-skins cleanly across
 * the LEAF / TERRACOTTA / SLATE candidates rather than hardcoding pastels.
 */
@Composable
fun HomeScreen(
    recentPlants: List<MyPlantRow>,
    onIdentify: () -> Unit,
    onMyPlants: () -> Unit,
    onBrowseMixes: () -> Unit,
    onAbout: () -> Unit,
    onRecentClick: (MyPlantRow) -> Unit,
    onOpenThemeSwitcher: () -> Unit = {},
    // Debug-only theme switcher entry (relocated off the camera). Excluded from release; parameterised
    // so the exclusion is deterministically testable.
    showDebugAffordances: Boolean = com.darkfactory.plantpotting.BuildConfig.DEBUG,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // Greeting header + decorative avatar.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.home_greeting),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(id = R.string.home_greeting_sub),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.testTag(HomeTags.TITLE),
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_plant_placeholder),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2×2 tile grid.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionTile(
                label = stringResource(id = R.string.home_identify),
                icon = Icons.Filled.Add,
                container = MaterialTheme.colorScheme.primaryContainer,
                onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                tag = HomeTags.IDENTIFY,
                onClick = onIdentify,
                modifier = Modifier.weight(1f),
            )
            ActionTile(
                label = stringResource(id = R.string.home_my_plants),
                icon = Icons.AutoMirrored.Filled.List,
                container = MaterialTheme.colorScheme.secondaryContainer,
                onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                tag = HomeTags.MY_PLANTS,
                onClick = onMyPlants,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionTile(
                label = stringResource(id = R.string.home_browse_mixes),
                icon = Icons.Filled.Search,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                tag = HomeTags.BROWSE_MIXES,
                onClick = onBrowseMixes,
                modifier = Modifier.weight(1f),
            )
            ActionTile(
                label = stringResource(id = R.string.home_about),
                icon = Icons.Filled.Info,
                container = MaterialTheme.colorScheme.surfaceVariant,
                onContainer = MaterialTheme.colorScheme.onSurfaceVariant,
                tag = HomeTags.ABOUT,
                onClick = onAbout,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Recent plants carousel.
        Text(
            text = stringResource(id = R.string.home_recent_heading),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (recentPlants.isEmpty()) {
            Text(
                text = stringResource(id = R.string.home_recent_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.testTag(HomeTags.RECENT_EMPTY),
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag(HomeTags.RECENT_ROW),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(recentPlants, key = { it.speciesId }) { plant ->
                    RecentPlantCard(plant = plant, onClick = { onRecentClick(plant) })
                }
            }
        }

        if (showDebugAffordances) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.home_debug_theme),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onOpenThemeSwitcher)
                        .padding(8.dp)
                        .testTag(HomeTags.THEME_SWITCHER_ENTRY),
            )
        }
    }
}

@Composable
private fun ActionTile(
    label: String,
    icon: ImageVector,
    container: Color,
    onContainer: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        modifier = modifier.aspectRatio(1.1f).testTag(tag),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun RecentPlantCard(
    plant: MyPlantRow,
    onClick: () -> Unit,
) {
    val hasRealImage = com.darkfactory.plantpotting.result.PlantImageResolver.hasRealImage(plant.speciesId)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.width(140.dp).testTag(HomeTags.recentCardTag(plant.speciesId)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = com.darkfactory.plantpotting.result.PlantImageResolver.drawableFor(plant.speciesId)),
                contentDescription = null,
                colorFilter = if (hasRealImage) null else ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentScale = if (hasRealImage) androidx.compose.ui.layout.ContentScale.Crop else androidx.compose.ui.layout.ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plant.displayName.ifBlank { plant.speciesId },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

object HomeTags {
    const val TITLE = "home.title"
    const val IDENTIFY = "home.identify"
    const val MY_PLANTS = "home.myPlants"
    const val BROWSE_MIXES = "home.browseMixes"
    const val ABOUT = "home.about"
    const val RECENT_ROW = "home.recentRow"
    const val RECENT_EMPTY = "home.recentEmpty"
    const val THEME_SWITCHER_ENTRY = "home.themeSwitcherEntry"

    fun recentCardTag(speciesId: String): String = "home.recent.$speciesId"
}
