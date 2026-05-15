package com.darkfactory.plantpotting.identify.model

import android.content.res.AssetManager
import com.darkfactory.plantpotting.identify.IdentificationFailureException
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Thin abstraction over `org.tensorflow.lite.Interpreter` so the score-mapping path
 * can be unit-tested on JVM without instantiating the native TFLite runtime.
 *
 * Production binds the [TfLiteInterpreterFacade]; tests construct a [FakeInterpreterFacade]
 * with canned scores. PLANTPOTTING-0003 §3.5 / PLANTPOTTING-0004 §1.7.
 */
interface InterpreterFacade {
    val labelCount: Int

    fun runInference(input: PreprocessedImage): FloatArray

    fun close()
}

/**
 * Production implementation. Lazily loads the `.tflite` from the AssetManager using a
 * memory-mapped `AssetFileDescriptor` channel so the model is paged in on demand and
 * shared cleanly across `identify(jpeg)` calls.
 *
 * Asserts at load time that the runtime input tensor's dtype matches
 * [expectedInputDtype]; mismatch throws [IdentificationFailureException] with a
 * diagnostic message. PLANTPOTTING-0004 §1.7 / Risk §7.5.
 *
 * For UINT8 models the output tensor is also UINT8 — `runInference` dequantizes the
 * `ByteArray` output via the model's quantization params (`scale`, `zeroPoint`) before
 * returning a `FloatArray` to [ModelScoreMapper], whose contract is unchanged.
 * PLANTPOTTING-0004 §1.9 / Decision §4.3.
 */
class TfLiteInterpreterFacade(
    private val assets: AssetManager,
    private val modelPath: String,
    override val labelCount: Int,
    private val inputSize: Int,
    private val expectedInputDtype: ModelDtype,
) : InterpreterFacade {
    private val interpreter: Interpreter by lazy { loadInterpreter() }

    private fun loadInterpreter(): Interpreter {
        val tflite =
            try {
                val fd = assets.openFd(modelPath)
                val channel = FileInputStream(fd.fileDescriptor).channel
                val buffer: MappedByteBuffer =
                    channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        fd.startOffset,
                        fd.declaredLength,
                    )
                Interpreter(buffer)
            } catch (e: Throwable) {
                throw IdentificationFailureException(
                    "Failed to load on-device model at $modelPath: ${e.message}",
                    e,
                )
            }
        val runtimeDtype = tflite.getInputTensor(0).dataType()
        val expected =
            when (expectedInputDtype) {
                ModelDtype.UINT8 -> DataType.UINT8
                ModelDtype.FLOAT32 -> DataType.FLOAT32
            }
        if (runtimeDtype != expected) {
            tflite.close()
            throw IdentificationFailureException(
                "Model input dtype mismatch: manifest=$expectedInputDtype, runtime=$runtimeDtype",
            )
        }
        return tflite
    }

    override fun runInference(input: PreprocessedImage): FloatArray {
        require(input.width == inputSize && input.height == inputSize) {
            "Expected ${inputSize}x$inputSize input but got ${input.width}x${input.height}"
        }
        return try {
            when (expectedInputDtype) {
                ModelDtype.UINT8 -> runUint8(input.buffer)
                ModelDtype.FLOAT32 -> runFloat32(input.buffer)
            }
        } catch (e: IdentificationFailureException) {
            throw e
        } catch (e: Throwable) {
            throw IdentificationFailureException(
                "TFLite inference failed: ${e.message}",
                e,
            )
        }
    }

    private fun runUint8(buffer: ByteBuffer): FloatArray {
        val output = Array(1) { ByteArray(labelCount) }
        interpreter.run(buffer, output)
        // AIY V1/3 output tensor is UINT8; dequantize via the model's quantizationParams
        // before ModelScoreMapper sees it.
        val qp = interpreter.getOutputTensor(0).quantizationParams()
        val scale = qp.scale
        val zeroPoint = qp.zeroPoint
        val raw = output[0]
        val floats = FloatArray(labelCount)
        for (i in 0 until labelCount) {
            val unsigned = raw[i].toInt() and 0xFF
            floats[i] = (unsigned - zeroPoint) * scale
        }
        return floats
    }

    private fun runFloat32(buffer: ByteBuffer): FloatArray {
        val output = Array(1) { FloatArray(labelCount) }
        interpreter.run(buffer, output)
        return output[0]
    }

    override fun close() {
        runCatching { interpreter.close() }
    }
}

/**
 * In-memory fake for unit tests. Returns the same canned scores irrespective of the
 * input image, so the score-mapping pipeline can be exercised deterministically without
 * native TFLite. The fake's contract is "ignore the input, return canned `FloatArray`
 * scores" — the same shape `ModelScoreMapper` consumes from the real facade after
 * UINT8-output dequantization (PLANTPOTTING-0004 §1.8 / Risk §7.4).
 */
class FakeInterpreterFacade(
    override val labelCount: Int,
    private var cannedScores: FloatArray,
    val expectedInputDtype: ModelDtype = ModelDtype.UINT8,
) : InterpreterFacade {
    init {
        require(cannedScores.size == labelCount) {
            "Canned scores length ${cannedScores.size} does not match labelCount $labelCount"
        }
    }

    var lastInput: PreprocessedImage? = null
        private set
    var lastInputBufferCapacity: Int = -1
        private set
    var closeCount: Int = 0
        private set

    fun setScores(scores: FloatArray) {
        require(scores.size == labelCount) {
            "Canned scores length ${scores.size} does not match labelCount $labelCount"
        }
        cannedScores = scores
    }

    override fun runInference(input: PreprocessedImage): FloatArray {
        lastInput = input
        lastInputBufferCapacity = input.buffer.capacity()
        return cannedScores.copyOf()
    }

    override fun close() {
        closeCount++
    }
}
