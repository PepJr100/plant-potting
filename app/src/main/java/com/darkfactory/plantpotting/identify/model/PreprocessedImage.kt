package com.darkfactory.plantpotting.identify.model

/**
 * Result of [ImagePreprocessor.preprocess]: a square RGB tensor whose interleaved
 * float values have been resized and normalised per the model manifest's input contract.
 *
 * `normalisedRgb` is a contiguous `[H * W * 3]` array — row-major, `(R,G,B)` per pixel —
 * ready to be wrapped as a TFLite input tensor.
 */
data class PreprocessedImage(
    val width: Int,
    val height: Int,
    val normalisedRgb: FloatArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PreprocessedImage) return false
        return width == other.width &&
            height == other.height &&
            normalisedRgb.contentEquals(other.normalisedRgb)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + normalisedRgb.contentHashCode()
        return result
    }
}
