package com.darkfactory.plantpotting.persistence

import kotlinx.coroutines.flow.Flow

/**
 * The app's first local-persistence layer (PLANTPOTTING-0010 D1). **One** store, two narrow
 * domain APIs — saved plants and add-request totals — so the "one layer, two collections" intent
 * is honoured without two storage mechanisms.
 *
 * The store stamps its own timestamps from an injected [TimeProvider]; callers never pass a clock.
 */
interface PlantLogStore {
    /** Saved **My Plants**, most-recent first, capped to the most-recent-N. */
    val identifiedPlants: Flow<List<IdentifiedPlant>>

    /** Add-this-plant request totals, one row per model class. */
    val addPlantRequests: Flow<List<AddPlantRequest>>

    /** PLANTPOTTING-0010 D5 — persisted debug theme-candidate name (defaults to "LEAF"). */
    val themeCandidate: Flow<String>

    /**
     * Append a user-saved plant to **My Plants** (most-recent first), evicting the oldest beyond
     * the N-cap. User-initiated only — never auto-append on every scan.
     */
    suspend fun saveIdentifiedPlant(
        speciesId: String,
        displayName: String,
        source: String,
        confidencePct: Int? = null,
    )

    /**
     * Record one "Add this plant" request for [modelClassLabel]: increment its counter (or create
     * the row at count 1) and refresh its `lastRequestedEpochMs`.
     */
    suspend fun recordAddPlantRequest(modelClassLabel: String)

    /** PLANTPOTTING-0010 D5 — persist the selected debug theme candidate. */
    suspend fun setThemeCandidate(name: String)

    /** One-shot read of the full document (used by restart round-trip tests). */
    suspend fun snapshot(): PlantLogDocument
}
