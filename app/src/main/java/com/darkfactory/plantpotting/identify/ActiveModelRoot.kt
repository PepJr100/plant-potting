package com.darkfactory.plantpotting.identify

import javax.inject.Qualifier

/**
 * Hilt qualifier for the active on-device model's assets root (e.g. `ml/aiy_plants_v1`).
 *
 * PLANTPOTTING-0007 §Phase 5 — the single `ACTIVE_MODEL_ROOT` selection point. Production
 * binds `BuildConfig.ACTIVE_MODEL_ROOT`; the manifest/labels/mapping/model providers all read
 * `"$root/..."` so the whole on-device path follows one switch. Instrumentation tests can
 * override this binding to point the production graph at a different bundle without a rebuild.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActiveModelRoot
