package com.darkfactory.plantpotting.kb

import android.content.res.AssetManager
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.Species
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class KbLoader @Inject constructor(
    private val assets: AssetManager,
    private val json: Json = DefaultJson,
) {
    @Volatile
    private var cached: KnowledgeBase? = null

    suspend fun load(): KnowledgeBase {
        cached?.let { return it }
        return withContext(Dispatchers.IO) {
            val archetypes: List<Archetype> = readJsonList(
                path = "kb/archetypes.json",
                serializer = ListSerializer(Archetype.serializer()),
            )
            val species: List<Species> = readJsonList(
                path = "kb/species.json",
                serializer = ListSerializer(Species.serializer()),
            )
            val kb = KbValidator.validate(archetypes, species)
            cached = kb
            kb
        }
    }

    private fun <T> readJsonList(
        path: String,
        serializer: kotlinx.serialization.KSerializer<List<T>>,
    ): List<T> {
        val text = assets.open(path).use { it.bufferedReader(Charsets.UTF_8).readText() }
        return json.decodeFromString(serializer, text)
    }

    companion object {
        val DefaultJson: Json = Json {
            ignoreUnknownKeys = false
            isLenient = false
            classDiscriminator = "kind"
            prettyPrint = false
        }
    }
}
