package com.darkfactory.plantpotting.ui.navigation

import com.darkfactory.plantpotting.identify.IdSource

/**
 * Compose-Navigation route shapes.
 *
 * `RESULT` accepts optional `source` and `lowConfidence` query args so the camera flow
 * can carry the §4.3 identifier outcome through to the badge dispatch (PLANTPOTTING-0003
 * §5.4). Defaults preserve legacy callers that only know the species id.
 */
object Routes {
    const val PERMISSION = "permission"
    const val CAMERA = "camera"
    const val RESULT = "result/{speciesId}?source={source}&lowConfidence={lowConfidence}"
    const val RECOMMENDATION = "recommendation/{speciesId}"
    const val LOW_CONFIDENCE_PICKER = "low-confidence-picker?candidates={candidates}"
    const val ARCHETYPE_PICKER = "archetype-picker"
    const val ARCHETYPE_RECOMMENDATION = "archetype-recommendation/{archetypeId}"

    const val ARG_SPECIES_ID = "speciesId"
    const val ARG_SOURCE = "source"
    const val ARG_LOW_CONFIDENCE = "lowConfidence"
    const val ARG_CANDIDATES = "candidates"
    const val ARG_ARCHETYPE_ID = "archetypeId"

    fun result(
        speciesId: String,
        source: IdSource = IdSource.STUB_DETERMINISTIC,
        lowConfidence: Boolean = false,
    ): String {
        val encoded = java.net.URLEncoder.encode(speciesId, "UTF-8")
        return "result/$encoded?source=${source.name}&lowConfidence=$lowConfidence"
    }

    fun recommendation(speciesId: String): String = "recommendation/${java.net.URLEncoder.encode(speciesId, "UTF-8")}"

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
