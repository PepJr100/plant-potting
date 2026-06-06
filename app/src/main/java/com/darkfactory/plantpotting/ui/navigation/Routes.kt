package com.darkfactory.plantpotting.ui.navigation

import com.darkfactory.plantpotting.identify.IdSource

/**
 * Compose-Navigation route shapes.
 *
 * `RESULT` accepts optional `source` and `lowConfidence` query args so the camera flow
 * can carry the §4.3 identifier outcome through to the badge dispatch (PLANTPOTTING-0003
 * §5.4). PLANTPOTTING-0010 D2 adds an optional `confidencePct` (0..100; `-1` ⇒ absent) so the
 * on-device high-confidence path can render a numeric confidence + bar without touching the
 * frozen `PlantIdentifier`/`IdentificationResult` seam. Defaults preserve legacy callers that
 * only know the species id.
 */
object Routes {
    /** Sentinel for an absent `confidencePct` (stub/picker flows carry no probability). */
    const val CONFIDENCE_ABSENT = -1

    /** PLANTPOTTING-0010 (review feedback) — landing screen: My Plants + Identify new plant. */
    const val HOME = "home"
    const val PERMISSION = "permission"
    const val CAMERA = "camera"
    const val RESULT = "result/{speciesId}?source={source}&lowConfidence={lowConfidence}&confidencePct={confidencePct}"
    const val RECOMMENDATION = "recommendation/{speciesId}"
    const val LOW_CONFIDENCE_PICKER = "low-confidence-picker?candidates={candidates}"
    const val ARCHETYPE_PICKER = "archetype-picker"
    const val ARCHETYPE_RECOMMENDATION = "archetype-recommendation/{archetypeId}"
    const val MY_PLANTS = "my-plants"
    const val ADD_THIS_PLANT = "add-this-plant?label={label}&confidencePct={confidencePct}"

    /** PLANTPOTTING-0010 D5 — debug-only theme switcher (reached from a BuildConfig.DEBUG affordance). */
    const val THEME_SWITCHER = "debug-theme-switcher"

    const val ARG_SPECIES_ID = "speciesId"
    const val ARG_SOURCE = "source"
    const val ARG_LOW_CONFIDENCE = "lowConfidence"
    const val ARG_CONFIDENCE_PCT = "confidencePct"
    const val ARG_CANDIDATES = "candidates"
    const val ARG_ARCHETYPE_ID = "archetypeId"
    const val ARG_MODEL_CLASS_LABEL = "label"

    fun result(
        speciesId: String,
        source: IdSource = IdSource.STUB_DETERMINISTIC,
        lowConfidence: Boolean = false,
        confidencePct: Int? = null,
    ): String {
        val encoded = java.net.URLEncoder.encode(speciesId, "UTF-8")
        val pct = confidencePct ?: CONFIDENCE_ABSENT
        return "result/$encoded?source=${source.name}&lowConfidence=$lowConfidence&confidencePct=$pct"
    }

    fun recommendation(speciesId: String): String = "recommendation/${java.net.URLEncoder.encode(speciesId, "UTF-8")}"

    /** PLANTPOTTING-0010 Pillar B — the "Add this plant" wireframe for a confident-but-unmapped class. */
    fun addThisPlant(
        modelClassLabel: String,
        confidencePct: Int,
    ): String {
        val encoded = java.net.URLEncoder.encode(modelClassLabel, "UTF-8")
        return "add-this-plant?label=$encoded&confidencePct=$confidencePct"
    }

    fun archetypeRecommendation(archetypeId: String): String =
        "archetype-recommendation/${java.net.URLEncoder.encode(archetypeId, "UTF-8")}"

    /**
     * Encodes up to 3 candidates as `speciesId|probPct[,speciesId|probPct]...`. The
     * integer percentage form keeps URL-encoded length under ~100 chars even with
     * the longest KB ids — well inside Compose-Navigation's argument tolerance.
     */
    fun lowConfidencePicker(candidates: List<com.darkfactory.plantpotting.identify.model.Candidate>): String {
        val encoded =
            candidates
                .take(3)
                .joinToString(",") { c ->
                    val pct = (c.probability * 100).toInt().coerceIn(0, 100)
                    "${c.speciesId}|$pct"
                }
        val arg = java.net.URLEncoder.encode(encoded, "UTF-8")
        return "low-confidence-picker?candidates=$arg"
    }
}
