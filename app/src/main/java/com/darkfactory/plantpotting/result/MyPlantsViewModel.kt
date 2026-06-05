package com.darkfactory.plantpotting.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.persistence.PlantLogStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * PLANTPOTTING-0010 Phase 4 — drives the **My Plants** folder from the shared [PlantLogStore].
 * Rows are most-recent first (the store already orders them that way). The screen shows the saved
 * name, a source badge, the confidence (if any), and the saved time; an empty state when none.
 */
@HiltViewModel
class MyPlantsViewModel
    @Inject
    constructor(
        store: PlantLogStore,
    ) : ViewModel() {
        val state: StateFlow<MyPlantsUiState> =
            store.identifiedPlants
                .map { plants ->
                    MyPlantsUiState(
                        rows =
                            plants.map { p ->
                                MyPlantRow(
                                    speciesId = p.speciesId,
                                    displayName = p.displayName,
                                    source = p.source.toIdSourceOrDefault(),
                                    confidencePct = p.confidencePct,
                                    savedAtEpochMs = p.savedAtEpochMs,
                                )
                            },
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = MyPlantsUiState(),
                )

        private fun String.toIdSourceOrDefault(): IdSource =
            runCatching { IdSource.valueOf(this) }.getOrDefault(IdSource.ON_DEVICE_MODEL)
    }

data class MyPlantsUiState(
    val rows: List<MyPlantRow> = emptyList(),
) {
    val isEmpty: Boolean get() = rows.isEmpty()
}

data class MyPlantRow(
    val speciesId: String,
    val displayName: String,
    val source: IdSource,
    val confidencePct: Int?,
    val savedAtEpochMs: Long,
)
