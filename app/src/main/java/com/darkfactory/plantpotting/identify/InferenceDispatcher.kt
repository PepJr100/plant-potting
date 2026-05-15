package com.darkfactory.plantpotting.identify

import javax.inject.Qualifier

/**
 * Hilt qualifier for the dispatcher that owns the on-device inference work.
 * Production binds this to `Dispatchers.Default`; tests can rebind it to a deterministic
 * dispatcher (e.g., `UnconfinedTestDispatcher`) via a `@TestInstallIn` module.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InferenceDispatcher
