package com.darkfactory.plantpotting.ui.theme

import androidx.compose.ui.graphics.Color

// --- LEAF (production default — unchanged from pre-0010) -----------------------
val LeafGreen = Color(0xFF1B5E20)
val LeafGreenLight = Color(0xFF4C8C4A)
val LeafGreenDark = Color(0xFF003300)
val Cream = Color(0xFFF8F4E3)
val Bark = Color(0xFF6D4C41)

// --- TERRACOTTA (warm clay / pot palette) -------------------------------------
val Terracotta = Color(0xFFB5651D)
val TerracottaLight = Color(0xFFE2925A)
val TerracottaDark = Color(0xFF7A3F12)
val SandShell = Color(0xFFFBF3E8)
val Olive = Color(0xFF6B6B3A)

// --- SLATE (cool, modern minimalist) ------------------------------------------
val SlateTeal = Color(0xFF00695C)
val SlateTealLight = Color(0xFF4DB6AC)
val SlateTealDark = Color(0xFF003D33)
val MistGrey = Color(0xFFF1F4F4)
val Graphite = Color(0xFF37474F)

/**
 * PLANTPOTTING-0010 D5 — selectable Compose theme candidates. The principal picks from real
 * on-device renders (debug switcher + screenshots); the production default stays [LEAF] until then.
 */
enum class ThemeCandidate {
    LEAF,
    TERRACOTTA,
    SLATE,
    ;

    companion object {
        /** Resolve a persisted theme name, falling back to the production default. */
        fun fromName(name: String?): ThemeCandidate =
            entries.firstOrNull { it.name == name } ?: LEAF
    }
}
