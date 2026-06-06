package com.darkfactory.plantpotting.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// --- LEAF (production default) -------------------------------------------------
private val LeafLight =
    lightColorScheme(
        primary = LeafGreen,
        onPrimary = Cream,
        secondary = Bark,
        onSecondary = Cream,
        background = Cream,
        onBackground = LeafGreenDark,
    )

private val LeafDark =
    darkColorScheme(
        primary = LeafGreenLight,
        onPrimary = LeafGreenDark,
        secondary = Bark,
        onSecondary = Cream,
    )

// --- TERRACOTTA ----------------------------------------------------------------
private val TerracottaLightScheme =
    lightColorScheme(
        primary = Terracotta,
        onPrimary = SandShell,
        secondary = Olive,
        onSecondary = SandShell,
        background = SandShell,
        onBackground = TerracottaDark,
    )

private val TerracottaDarkScheme =
    darkColorScheme(
        primary = TerracottaLight,
        onPrimary = TerracottaDark,
        secondary = Olive,
        onSecondary = SandShell,
    )

// --- SLATE ---------------------------------------------------------------------
private val SlateLightScheme =
    lightColorScheme(
        primary = SlateTeal,
        onPrimary = MistGrey,
        secondary = Graphite,
        onSecondary = MistGrey,
        background = MistGrey,
        onBackground = SlateTealDark,
    )

private val SlateDarkScheme =
    darkColorScheme(
        primary = SlateTealLight,
        onPrimary = SlateTealDark,
        secondary = Graphite,
        onSecondary = MistGrey,
    )

private fun schemeFor(
    candidate: ThemeCandidate,
    dark: Boolean,
): ColorScheme =
    when (candidate) {
        ThemeCandidate.LEAF -> if (dark) LeafDark else LeafLight
        ThemeCandidate.TERRACOTTA -> if (dark) TerracottaDarkScheme else TerracottaLightScheme
        ThemeCandidate.SLATE -> if (dark) SlateDarkScheme else SlateLightScheme
    }

/**
 * PLANTPOTTING-0010 D5 — parameterised by [candidate] so the debug switcher can flip palettes
 * on-device. Default stays [ThemeCandidate.LEAF] (the production scheme) until the principal picks.
 */
@Composable
fun PlantPottingTheme(
    candidate: ThemeCandidate = ThemeCandidate.LEAF,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = schemeFor(candidate, useDarkTheme), content = content)
}
