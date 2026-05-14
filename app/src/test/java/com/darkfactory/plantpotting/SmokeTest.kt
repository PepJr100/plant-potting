package com.darkfactory.plantpotting

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Wires up the JVM unit-test runner. If `./gradlew testDebugUnitTest` discovers and
 * runs this, the test infrastructure is healthy.
 */
class SmokeTest {
    @Test
    fun runnerIsWired() {
        assertThat(2 + 2).isEqualTo(4)
    }
}
