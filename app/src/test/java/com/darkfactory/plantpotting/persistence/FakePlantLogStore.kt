package com.darkfactory.plantpotting.persistence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * In-memory [PlantLogStore] for unit tests — same semantics as [DataStorePlantLogStore]
 * (most-recent-first My Plants with an N-cap, per-class request totals) without any file I/O.
 */
class FakePlantLogStore(
    private val nowEpochMs: Long = 0L,
    private val maxIdentifiedPlants: Int = 100,
) : PlantLogStore {
    private val doc = MutableStateFlow(PlantLogDocument())

    override val identifiedPlants: Flow<List<IdentifiedPlant>> = doc.map { it.identifiedPlants }
    override val addPlantRequests: Flow<List<AddPlantRequest>> = doc.map { it.addPlantRequests }
    override val themeCandidate: Flow<String> = doc.map { it.themeCandidate }

    override suspend fun saveIdentifiedPlant(
        speciesId: String,
        displayName: String,
        source: String,
        confidencePct: Int?,
    ) {
        val entry = IdentifiedPlant(speciesId, displayName, source, confidencePct, nowEpochMs)
        doc.value =
            doc.value.copy(
                identifiedPlants = (listOf(entry) + doc.value.identifiedPlants).take(maxIdentifiedPlants),
            )
    }

    override suspend fun recordAddPlantRequest(modelClassLabel: String) {
        val existing = doc.value.addPlantRequests.firstOrNull { it.modelClassLabel == modelClassLabel }
        val updated =
            if (existing == null) {
                doc.value.addPlantRequests + AddPlantRequest(modelClassLabel, 1, nowEpochMs)
            } else {
                doc.value.addPlantRequests.map {
                    if (it.modelClassLabel == modelClassLabel) {
                        it.copy(count = it.count + 1, lastRequestedEpochMs = nowEpochMs)
                    } else {
                        it
                    }
                }
            }
        doc.value = doc.value.copy(addPlantRequests = updated)
    }

    override suspend fun setThemeCandidate(name: String) {
        doc.value = doc.value.copy(themeCandidate = name)
    }

    override suspend fun snapshot(): PlantLogDocument = doc.first()
}
