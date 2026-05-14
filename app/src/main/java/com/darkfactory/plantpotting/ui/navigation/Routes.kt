package com.darkfactory.plantpotting.ui.navigation

object Routes {
    const val PERMISSION = "permission"
    const val CAMERA = "camera"
    const val RESULT = "result/{speciesId}"
    const val RECOMMENDATION = "recommendation/{speciesId}"

    fun result(speciesId: String): String = "result/${java.net.URLEncoder.encode(speciesId, "UTF-8")}"

    fun recommendation(speciesId: String): String = "recommendation/${java.net.URLEncoder.encode(speciesId, "UTF-8")}"

    const val ARG_SPECIES_ID = "speciesId"
}
