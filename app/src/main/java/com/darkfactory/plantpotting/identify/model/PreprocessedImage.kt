package com.darkfactory.plantpotting.identify.model

import java.nio.ByteBuffer

/**
 * Result of [ImagePreprocessor.preprocess]: a square RGB tensor sized to the model
 * manifest's input contract. The buffer is the natural shape for both UINT8 and FLOAT32
 * paths (`TensorImage.buffer` in tensorflow-lite-support) and feeds straight into
 * [org.tensorflow.lite.Interpreter.run].
 *
 * - UINT8 path: `buffer.capacity() == width * height * 3` (one byte per channel).
 * - FLOAT32 path: `buffer.capacity() == width * height * 3 * 4` (four bytes per channel).
 *
 * PLANTPOTTING-0004 Decision §4.2 — replaces the previous `normalisedRgb: FloatArray`
 * shape so the same data class can carry either dtype without lying about its contents.
 */
data class PreprocessedImage(
    val width: Int,
    val height: Int,
    val buffer: ByteBuffer,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PreprocessedImage) return false
        return width == other.width &&
            height == other.height &&
            buffer == other.buffer
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + buffer.hashCode()
        return result
    }
}
