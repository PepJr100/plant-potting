package com.darkfactory.plantpotting.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Cream,
    secondary = Bark,
    onSecondary = Cream,
    background = Cream,
    onBackground = LeafGreenDark,
)

private val DarkColors = darkColorScheme(
    primary = LeafGreenLight,
    onPrimary = LeafGreenDark,
    secondary = Bark,
    onSecondary = Cream,
)

@Composable
fun PlantPottingTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
