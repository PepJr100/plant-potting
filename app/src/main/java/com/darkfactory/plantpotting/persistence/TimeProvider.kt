package com.darkfactory.plantpotting.persistence

import javax.inject.Inject

/**
 * Injectable wall-clock so the persistence layer is unit-testable with a fake clock
 * (PLANTPOTTING-0010 D1). Production binds [SystemTimeProvider]; tests supply a lambda
 * (e.g. `TimeProvider { 1_000L }`).
 */
fun interface TimeProvider {
    fun nowEpochMs(): Long
}

/** Production [TimeProvider] backed by the system clock. */
class SystemTimeProvider
    @Inject
    constructor() : TimeProvider {
        override fun nowEpochMs(): Long = System.currentTimeMillis()
    }
