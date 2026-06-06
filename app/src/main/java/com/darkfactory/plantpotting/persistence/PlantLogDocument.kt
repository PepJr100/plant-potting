package com.darkfactory.plantpotting.persistence

import kotlinx.serialization.Serializable

/**
 * The single on-device persistence document (PLANTPOTTING-0010 D1). One DataStore file,
 * two logical collections exposed through the [PlantLogStore] narrow accessors:
 *
 *  - [identifiedPlants] backs the **My Plants** folder (Phase 4), most-recent first, N-capped.
 *  - [addPlantRequests] backs the **Add this plant** request log (Phase 5), totalled per class.
 *
 * Reuses the existing `kotlinx.serialization.json` — no Room, no KSP/schema/migration ceremony
 * (Room was explicitly rejected for this append-mostly data shape).
 */
@Serializable
data class PlantLogDocument(
    val identifiedPlants: List<IdentifiedPlant> = emptyList(),
    val addPlantRequests: List<AddPlantRequest> = emptyList(),
    // PLANTPOTTING-0010 D5 — debug-only theme-candidate selection, persisted in the same DataStore
    // ("one local store"). Defaults to the production "LEAF" scheme. Release builds never write it.
    val themeCandidate: String = "LEAF",
)

/**
 * A plant the user explicitly saved to **My Plants**. [source] is the [com.darkfactory.plantpotting.identify.IdSource]
 * name (stored as a string so this DTO does not pull the identify seam into the persistence layer).
 * [confidencePct] is the integer percentage from the on-device high-confidence path, or null for
 * stub / picker flows that carry no probability.
 */
@Serializable
data class IdentifiedPlant(
    val speciesId: String,
    val displayName: String,
    val source: String,
    val confidencePct: Int? = null,
    val savedAtEpochMs: Long,
)

/**
 * A totalled request for a model class that has no KB entry (PLANTPOTTING-0010 Pillar B). Each
 * "Add this plant" tap increments [count] and refreshes [lastRequestedEpochMs]. Local-only — the
 * principal collects these totals off-device later; there is no export/sync.
 */
@Serializable
data class AddPlantRequest(
    val modelClassLabel: String,
    val count: Int,
    val lastRequestedEpochMs: Long,
)
