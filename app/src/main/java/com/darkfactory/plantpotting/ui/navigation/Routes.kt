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

    const val ARG_SPECIES_ID = "speciesId"
    const val ARG_SOURCE = "source"
    const val ARG_LOW_CONFIDENCE = "lowConfidence"

    fun result(
        speciesId: String,
        source: IdSource = IdSource.STUB_DETERMINISTIC,
        lowConfidence: Boolean = false,
    ): String {
        val encoded = java.net.URLEncoder.encode(speciesId, "UTF-8")
        return "result/$encoded?source=${source.name}&lowConfidence=$lowConfidence"
    }

    fun recommendation(speciesId: String): String = "recommendation/${java.net.URLEncoder.encode(speciesId, "UTF-8")}"
}
