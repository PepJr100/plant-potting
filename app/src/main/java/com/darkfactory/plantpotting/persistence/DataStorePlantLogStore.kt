package com.darkfactory.plantpotting.persistence

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [PlantLogStore] backed by a typed [DataStore] document (PLANTPOTTING-0010 D1). Append-mostly,
 * local-file only. Timestamps come from the injected [TimeProvider].
 *
 * @param maxIdentifiedPlants most-recent-N cap on the My Plants collection; the oldest entries are
 *   evicted on write so the file cannot grow unbounded.
 */
class DataStorePlantLogStore(
    private val dataStore: DataStore<PlantLogDocument>,
    private val timeProvider: TimeProvider,
    private val maxIdentifiedPlants: Int = DEFAULT_MAX_IDENTIFIED_PLANTS,
) : PlantLogStore {
    override val identifiedPlants: Flow<List<IdentifiedPlant>> =
        dataStore.data.map { it.identifiedPlants }

    override val addPlantRequests: Flow<List<AddPlantRequest>> =
        dataStore.data.map { it.addPlantRequests }

    override suspend fun saveIdentifiedPlant(
        speciesId: String,
        displayName: String,
        source: String,
        confidencePct: Int?,
    ) {
        val entry =
            IdentifiedPlant(
                speciesId = speciesId,
                displayName = displayName,
                source = source,
                confidencePct = confidencePct,
                savedAtEpochMs = timeProvider.nowEpochMs(),
            )
        dataStore.updateData { doc ->
            // Most-recent first, then evict beyond the N-cap.
            doc.copy(
                identifiedPlants = (listOf(entry) + doc.identifiedPlants).take(maxIdentifiedPlants),
            )
        }
    }

    override suspend fun recordAddPlantRequest(modelClassLabel: String) {
        val now = timeProvider.nowEpochMs()
        dataStore.updateData { doc ->
            val existing = doc.addPlantRequests.firstOrNull { it.modelClassLabel == modelClassLabel }
            val updated =
                if (existing == null) {
                    doc.addPlantRequests +
                        AddPlantRequest(
                            modelClassLabel = modelClassLabel,
                            count = 1,
                            lastRequestedEpochMs = now,
                        )
                } else {
                    doc.addPlantRequests.map { row ->
                        if (row.modelClassLabel == modelClassLabel) {
                            row.copy(count = row.count + 1, lastRequestedEpochMs = now)
                        } else {
                            row
                        }
                    }
                }
            doc.copy(addPlantRequests = updated)
        }
    }

    override suspend fun snapshot(): PlantLogDocument = dataStore.data.first()

    companion object {
        const val DEFAULT_MAX_IDENTIFIED_PLANTS = 100
    }
}
