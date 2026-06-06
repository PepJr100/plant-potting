package com.darkfactory.plantpotting.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.persistence.PlantLogStore
import com.darkfactory.plantpotting.result.MyPlantRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * PLANTPOTTING-0010 (review feedback / Dribbble layout) — backs the Home screen's "recent plants"
 * carousel with the most-recent saved plants from the shared [PlantLogStore].
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        store: PlantLogStore,
    ) : ViewModel() {
        val recentPlants: StateFlow<List<MyPlantRow>> =
            store.identifiedPlants
                .map { plants ->
                    plants.take(RECENT_LIMIT).map { p ->
                        MyPlantRow(
                            speciesId = p.speciesId,
                            displayName = p.displayName,
                            source = runCatching { IdSource.valueOf(p.source) }.getOrDefault(IdSource.ON_DEVICE_MODEL),
                            confidencePct = p.confidencePct,
                            savedAtEpochMs = p.savedAtEpochMs,
                        )
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        companion object {
            const val RECENT_LIMIT = 8
        }
    }
