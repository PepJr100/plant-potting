package com.darkfactory.plantpotting.identify.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.ByteBuffer

/**
 * PLANTPOTTING-0004 §0.5 contract lock — `PreprocessedImage` ships a `ByteBuffer`, not a
 * `FloatArray`. The buffer is the natural shape for both UINT8 and FLOAT32 paths and feeds
 * straight into `Interpreter.run`. RED today: the field is `normalisedRgb: FloatArray`.
 */
class PreprocessedImageBufferShapeContractTest {
    @Test
    fun preprocessedImageDeclaresWidthHeightAndByteBuffer() {
        val declared =
            PreprocessedImage::class.java.declaredFields
                .associate { it.name to it.type }
        assertThat(declared).containsEntry("width", Int::class.javaPrimitiveType)
        assertThat(declared).containsEntry("height", Int::class.javaPrimitiveType)
        assertThat(declared).containsEntry("buffer", ByteBuffer::class.java)
    }

    @Test
    fun preprocessedImageDoesNotDeclareNormalisedRgbField() {
        val declared = PreprocessedImage::class.java.declaredFields.map { it.name }
        assertThat(declared).doesNotContain("normalisedRgb")
    }
}
