package com.darkfactory.plantpotting.identify

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0011 Phase 1b — determinism + label-preservation guard for [FixturePerturbations].
 *
 * The accuracy scorecard treats perturbed copies as reproducible synthetic-robustness rows, so the
 * generator MUST be deterministic (same input → pixel-identical output) and MUST only transform
 * pixels (it never carries or changes a label — the expected `<kb-species-id>` is inherited from the
 * source fixture by the harness). These tests pin both properties.
 */
class FixturePerturbationsTest {
    private fun sampleBitmap(): Bitmap {
        // Deterministic non-uniform image so transforms produce distinguishable output.
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (y in 0 until size) {
            for (x in 0 until size) {
                bmp.setPixel(x, y, Color.rgb((x * 4) % 256, (y * 4) % 256, (x + y) % 256))
            }
        }
        return bmp
    }

    @Test
    fun emitsTheFixedDocumentedFamily() {
        val perturbations = FixturePerturbations.all(sampleBitmap())
        val kinds = perturbations.map { it.kind }
        assertThat(kinds).containsExactly(
            "blur",
            "crop_center_zoom",
            "crop_offcenter_zoom",
            "rotate_+15",
            "rotate_-15",
            "rotate_+90",
            "rotate_-90",
            "bright_up",
            "bright_down",
            "contrast_up",
            "contrast_down",
        )
        // Families used for the per-family scorecard cut.
        assertThat(perturbations.map { it.family }.toSet())
            .containsExactly("blur", "crop", "rotate", "brightness", "contrast")
    }

    @Test
    fun everyPerturbationDecodesToNonzeroDimensions() {
        FixturePerturbations.all(sampleBitmap()).forEach {
            assertThat(it.bitmap.width).isGreaterThan(0)
            assertThat(it.bitmap.height).isGreaterThan(0)
        }
    }

    @Test
    fun isDeterministicAcrossRuns() {
        val a = FixturePerturbations.all(sampleBitmap())
        val b = FixturePerturbations.all(sampleBitmap())
        assertThat(a.size).isEqualTo(b.size)
        for (i in a.indices) {
            assertThat(a[i].kind).isEqualTo(b[i].kind)
            // Bitmap.sameAs is a full pixel comparison — proves reproducibility.
            assertThat(a[i].bitmap.sameAs(b[i].bitmap)).isTrue()
        }
    }

    @Test
    fun perturbationsActuallyAlterThePixels() {
        // Label preservation is structural (no relabel); but each transform must change pixels,
        // otherwise it is not exercising robustness. The ±90° rotations of a non-symmetric image
        // and every photometric/blur/crop transform differ from the source.
        val src = sampleBitmap()
        FixturePerturbations.all(src).forEach {
            assertThat(it.bitmap.sameAs(src)).isFalse()
        }
    }
}
