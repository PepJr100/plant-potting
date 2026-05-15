package com.darkfactory.plantpotting.identify.model

import android.content.res.AssetManager
import com.darkfactory.plantpotting.identify.IdentificationFailureException
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Thin abstraction over `org.tensorflow.lite.Interpreter` so the score-mapping path
 * can be unit-tested on JVM without instantiating the native TFLite runtime.
 *
 * Production binds the [TfLiteInterpreterFacade]; tests construct a [FakeInterpreterFacade]
 * with canned scores. PLANTPOTTING-0003 §3.5.
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
 * Any failure during load or inference is wrapped in [IdentificationFailureException] —
 * native errors are *not* converted to low-confidence results (PLANTPOTTING-0003 §4.3
 * distinction).
 */
class TfLiteInterpreterFacade(
    private val assets: AssetManager,
    private val modelPath: String,
    override val labelCount: Int,
    private val inputSize: Int,
) : InterpreterFacade {
    private val interpreter: Interpreter by lazy { loadInterpreter() }

    private fun loadInterpreter(): Interpreter {
        try {
            val fd = assets.openFd(modelPath)
            val channel = FileInputStream(fd.fileDescriptor).channel
            val buffer: MappedByteBuffer =
                channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength,
                )
            return Interpreter(buffer)
        } catch (e: Throwable) {
            throw IdentificationFailureException(
                "Failed to load on-device model at $modelPath: ${e.message}",
                e,
            )
        }
    }

    override fun runInference(input: PreprocessedImage): FloatArray {
        require(input.width == inputSize && input.height == inputSize) {
            "Expected ${inputSize}x$inputSize input but got ${input.width}x${input.height}"
        }
        val inputTensor = reshape4d(input.normalisedRgb)
        val output = Array(1) { FloatArray(labelCount) }
        try {
            interpreter.run(inputTensor, output)
        } catch (e: Throwable) {
            throw IdentificationFailureException(
                "TFLite inference failed: ${e.message}",
                e,
            )
        }
        return output[0]
    }

    private fun reshape4d(flat: FloatArray): Array<Array<Array<FloatArray>>> {
        val out = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(3) } } }
        var i = 0
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                for (c in 0 until 3) {
                    out[0][y][x][c] = flat[i++]
                }
            }
        }
        return out
    }

    override fun close() {
        runCatching { interpreter.close() }
    }
}

/**
 * In-memory fake for unit tests. Returns the same canned scores irrespective of the
 * input image, so the score-mapping pipeline can be exercised deterministically without
 * native TFLite.
 */
class FakeInterpreterFacade(
    override val labelCount: Int,
    private var cannedScores: FloatArray,
) : InterpreterFacade {
    init {
        require(cannedScores.size == labelCount) {
            "Canned scores length ${cannedScores.size} does not match labelCount $labelCount"
        }
    }

    var lastInput: PreprocessedImage? = null
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
        return cannedScores.copyOf()
    }

    override fun close() {
        closeCount++
    }
}
