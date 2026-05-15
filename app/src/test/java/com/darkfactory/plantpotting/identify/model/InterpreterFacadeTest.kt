package com.darkfactory.plantpotting.identify.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * PLANTPOTTING-0004 §1.6 — locks the [InterpreterFacade] contract via its in-memory fake.
 * The real TFLite-backed `TfLiteInterpreterFacade`'s load-time dtype assertion is exercised
 * by the §3.2 instrumentation test (against the real interpreter) — JVM-side, instantiating
 * the real `Interpreter` is unreliable, so the unit suite locks the fake's `ByteBuffer`
 * contract only.
 */
class InterpreterFacadeTest {
    private fun uint8Image(): PreprocessedImage =
        PreprocessedImage(
            width = 224,
            height = 224,
            buffer = ByteBuffer.allocateDirect(224 * 224 * 3).order(ByteOrder.nativeOrder()),
        )

    private fun float32Image(): PreprocessedImage =
        PreprocessedImage(
            width = 224,
            height = 224,
            buffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder()),
        )

    @Test
    fun runInferenceAcceptsByteBuffer() {
        val fake = FakeInterpreterFacade(labelCount = 18, cannedScores = FloatArray(18) { 0.05f })
        val out = fake.runInference(uint8Image())
        assertThat(out).hasLength(18)
    }

    @Test
    fun fakeInterpreterFacadeRecordsBufferCapacityForBothDtypes() {
        val uint8Fake =
            FakeInterpreterFacade(
                labelCount = 18,
                cannedScores = FloatArray(18) { 0.05f },
                expectedInputDtype = ModelDtype.UINT8,
            )
        uint8Fake.runInference(uint8Image())
        assertThat(uint8Fake.lastInputBufferCapacity).isEqualTo(224 * 224 * 3)

        val float32Fake =
            FakeInterpreterFacade(
                labelCount = 18,
                cannedScores = FloatArray(18) { 0.05f },
                expectedInputDtype = ModelDtype.FLOAT32,
            )
        float32Fake.runInference(float32Image())
        assertThat(float32Fake.lastInputBufferCapacity).isEqualTo(224 * 224 * 3 * 4)
    }

    @Test
    fun fakeReturnsCannedScoresOfDeclaredLength() {
        val fake = FakeInterpreterFacade(labelCount = 18, cannedScores = FloatArray(18) { 0.05f })
        val out = fake.runInference(uint8Image())
        assertThat(out).hasLength(18)
    }

    @Test
    fun fakeReturnsSameValuesItWasSeededWith() {
        val seed = FloatArray(18) { it.toFloat() / 18f }
        val fake = FakeInterpreterFacade(labelCount = 18, cannedScores = seed)
        val out = fake.runInference(uint8Image())
        assertThat(out.toList()).isEqualTo(seed.toList())
    }

    @Test
    fun fakeRecordsTheLastInputForAssertions() {
        val fake = FakeInterpreterFacade(labelCount = 5, cannedScores = FloatArray(5))
        val img = uint8Image()
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
        val out = fake.runInference(uint8Image())
        assertThat(out[0]).isWithin(1e-6f).of(0.6f)
        assertThat(out[2]).isWithin(1e-6f).of(0.1f)
    }
}
