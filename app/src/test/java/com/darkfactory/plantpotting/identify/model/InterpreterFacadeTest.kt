package com.darkfactory.plantpotting.identify.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0003 §3.4 — locks the [InterpreterFacade] contract via its in-memory fake.
 * The real TFLite-backed `TfLiteInterpreterFacade` is exercised in the §4.3 instrumentation
 * test (binding check only) and indirectly via the failure-path test in §3.11.
 */
class InterpreterFacadeTest {
    private fun image(): PreprocessedImage =
        PreprocessedImage(
            width = 224,
            height = 224,
            normalisedRgb = FloatArray(224 * 224 * 3) { 0f },
        )

    @Test
    fun fakeReturnsCannedScoresOfDeclaredLength() {
        val fake = FakeInterpreterFacade(labelCount = 18, cannedScores = FloatArray(18) { 0.05f })
        val out = fake.runInference(image())
        assertThat(out).hasLength(18)
    }

    @Test
    fun fakeReturnsSameValuesItWasSeededWith() {
        val seed = FloatArray(18) { it.toFloat() / 18f }
        val fake = FakeInterpreterFacade(labelCount = 18, cannedScores = seed)
        val out = fake.runInference(image())
        assertThat(out.toList()).isEqualTo(seed.toList())
    }

    @Test
    fun fakeRecordsTheLastInputForAssertions() {
        val fake = FakeInterpreterFacade(labelCount = 5, cannedScores = FloatArray(5))
        val img = image()
        fake.runInference(img)
        assertThat(fake.lastInput).isSameInstanceAs(img)
    }

    @Test
    fun fakeRejectsCannedScoresOfWrongLength() {
        try {
            FakeInterpreterFacade(labelCount = 18, cannedScores = FloatArray(7))
            assertThat("did not throw").isEqualTo("threw")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("does not match labelCount")
        }
    }

    @Test
    fun closeIsIdempotentOnTheFake() {
        val fake = FakeInterpreterFacade(labelCount = 1, cannedScores = FloatArray(1) { 1f })
        fake.close()
        fake.close()
        fake.close()
        assertThat(fake.closeCount).isEqualTo(3)
    }

    @Test
    fun setScoresUpdatesNextInferenceOutput() {
        val fake = FakeInterpreterFacade(labelCount = 3, cannedScores = floatArrayOf(0.1f, 0.2f, 0.7f))
        fake.setScores(floatArrayOf(0.6f, 0.3f, 0.1f))
        val out = fake.runInference(image())
        assertThat(out[0]).isWithin(1e-6f).of(0.6f)
        assertThat(out[2]).isWithin(1e-6f).of(0.1f)
    }
}
