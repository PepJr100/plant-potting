package com.darkfactory.plantpotting.persistence

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * DataStore [Serializer] for [PlantLogDocument], reusing the project's `kotlinx.serialization.json`.
 * A corrupt/unreadable file surfaces as a [CorruptionException] so DataStore can recover to
 * [defaultValue] rather than crash the app.
 */
object PlantLogSerializer : Serializer<PlantLogDocument> {
    override val defaultValue: PlantLogDocument = PlantLogDocument()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(input: InputStream): PlantLogDocument {
        val text = input.readBytes().decodeToString()
        // A zero-byte / blank file is an empty store, not corruption — recover to the default.
        if (text.isBlank()) return defaultValue
        return try {
            json.decodeFromString(PlantLogDocument.serializer(), text)
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read PlantLogDocument", e)
        }
    }

    override suspend fun writeTo(
        t: PlantLogDocument,
        output: OutputStream,
    ) {
        output.write(
            json.encodeToString(PlantLogDocument.serializer(), t).encodeToByteArray(),
        )
    }
}
