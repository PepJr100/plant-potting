package com.darkfactory.plantpotting.identify.model

import android.content.res.AssetManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * In-memory projection of `plant_class_map.json`. Keys are stored in normalised form
 * (`trim().lowercase()`) so the runtime lookup against a predicted-class label string is
 * Unicode-insensitive to leading whitespace or trade-tag capitalisation.
 */
data class ModelLabelMap(
    val version: Int,
    val labelsAsset: String,
    private val byNormalisedLabel: Map<String, Entry>,
) {
    data class Entry(
        val kbSpeciesId: String,
        val alias: Boolean,
    )

    fun lookup(modelLabel: String): Entry? = byNormalisedLabel[normalise(modelLabel)]

    companion object {
        fun normalise(s: String): String = s.trim().lowercase()
    }
}

class ModelLabelMapReader(
    private val assets: AssetManager,
    private val dir: String = "ml/aiy_plants_v1",
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun read(): ModelLabelMap {
        val raw =
            assets
                .open("$dir/plant_class_map.json")
                .use { it.readBytes() }
                .let { String(it, Charsets.UTF_8) }
        val obj = json.parseToJsonElement(raw).jsonObject
        val version = obj["version"]!!.jsonPrimitive.content.toInt()
        val labelsAsset = obj["modelLabelsAsset"]!!.jsonPrimitive.content
        val entries = obj["mapping"]!!.jsonObject
        val map =
            entries.entries.associate { (label, row) ->
                val r = row.jsonObject
                val kb = r["kbSpeciesId"]!!.jsonPrimitive.content
                val alias = r["alias"]?.jsonPrimitive?.boolean ?: false
                ModelLabelMap.normalise(label) to ModelLabelMap.Entry(kbSpeciesId = kb, alias = alias)
            }
        return ModelLabelMap(version = version, labelsAsset = labelsAsset, byNormalisedLabel = map)
    }
}

/** Reader for the line-per-label labels.csv asset. */
class ModelLabelsReader(
    private val assets: AssetManager,
    private val path: String = "ml/aiy_plants_v1/labels.csv",
) {
    fun read(): List<String> =
        assets
            .open(path)
            .bufferedReader()
            .useLines { seq -> seq.map { it.trim() }.filter { it.isNotBlank() }.toList() }
}
