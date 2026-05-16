package com.darkfactory.plantpotting.identify

import javax.inject.Qualifier

/**
 * PLANTPOTTING-0005 §5.2 — Hilt qualifier for the per-species `highConfidencePlain` override
 * map. Keyed by KB `speciesId`; consulted by `ModelScoreMapper` before the global default.
 * Avoids colliding with any other `Map<String, Float>` binding in the graph.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PerSpeciesThresholds
