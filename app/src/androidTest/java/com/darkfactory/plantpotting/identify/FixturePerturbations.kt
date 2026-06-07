package com.darkfactory.plantpotting.identify

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint

/**
 * PLANTPOTTING-0011 Phase 1b — deterministic, in-process, network-free synthetic perturbations.
 *
 * Takes a decoded [Bitmap] and emits a **fixed, documented** family of stressed copies:
 *  - **blur** — a separable box blur (radius [BLUR_RADIUS]);
 *  - **crop** — centre zoom and an off-centre zoom (simulates framing error);
 *  - **rotate** — ±15° (slight tilt) and ±90° (orientation error);
 *  - **brightness** — ± a fixed luminance offset;
 *  - **contrast** — × a fixed contrast factor either side of 1.0.
 *
 * Android-graphics only (`Canvas` / `Matrix` / `ColorMatrix` / a hand-rolled box blur) — **no new
 * dependency, no network**. All parameters are **fixed constants** (no RNG), so the same input
 * bitmap always yields pixel-identical outputs: runs are reproducible.
 *
 * Each perturbation inherits the expected `<kb-species-id>` from its source fixture; the generator
 * never changes the label (it only transforms pixels). Results are reported as
 * *synthetic-robustness*, never as independent real-world samples.
 */
object FixturePerturbations {
    /** A single stressed copy plus the family it belongs to (for per-family scorecard cuts). */
    data class Perturbation(
        val kind: String,
        val family: String,
        val bitmap: Bitmap,
    )

    /** Fixed family applied to every fixture. Deterministic order; no RNG. */
    fun all(src: Bitmap): List<Perturbation> =
        listOf(
            Perturbation("blur", "blur", boxBlur(src, BLUR_RADIUS)),
            Perturbation("crop_center_zoom", "crop", zoomCrop(src, CROP_ZOOM, anchorFracX = 0.5f, anchorFracY = 0.5f)),
            Perturbation("crop_offcenter_zoom", "crop", zoomCrop(src, CROP_ZOOM, anchorFracX = 0.0f, anchorFracY = 0.0f)),
            Perturbation("rotate_+15", "rotate", rotate(src, 15f)),
            Perturbation("rotate_-15", "rotate", rotate(src, -15f)),
            Perturbation("rotate_+90", "rotate", rotate(src, 90f)),
            Perturbation("rotate_-90", "rotate", rotate(src, -90f)),
            Perturbation("bright_up", "brightness", colorMatrix(src, brightnessMatrix(BRIGHTNESS_DELTA))),
            Perturbation("bright_down", "brightness", colorMatrix(src, brightnessMatrix(-BRIGHTNESS_DELTA))),
            Perturbation("contrast_up", "contrast", colorMatrix(src, contrastMatrix(CONTRAST_UP))),
            Perturbation("contrast_down", "contrast", colorMatrix(src, contrastMatrix(CONTRAST_DOWN))),
        )

    // ---- transforms (all deterministic) ----

    private fun rotate(
        src: Bitmap,
        degrees: Float,
    ): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        // Black fill behind the rotation so off-axis corners are a neutral, deterministic colour.
        canvas.drawColor(android.graphics.Color.BLACK)
        val matrix = Matrix().apply { postRotate(degrees, src.width / 2f, src.height / 2f) }
        canvas.drawBitmap(src, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        return out
    }

    private fun zoomCrop(
        src: Bitmap,
        keepFraction: Float,
        anchorFracX: Float,
        anchorFracY: Float,
    ): Bitmap {
        val cw = (src.width * keepFraction).toInt().coerceAtLeast(1)
        val ch = (src.height * keepFraction).toInt().coerceAtLeast(1)
        val x = ((src.width - cw) * anchorFracX).toInt().coerceIn(0, src.width - cw)
        val y = ((src.height - ch) * anchorFracY).toInt().coerceIn(0, src.height - ch)
        val cropped = Bitmap.createBitmap(src, x, y, cw, ch)
        return Bitmap.createScaledBitmap(cropped, src.width, src.height, true)
    }

    private fun colorMatrix(
        src: Bitmap,
        cm: ColorMatrix,
    ): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun brightnessMatrix(delta: Float): ColorMatrix =
        ColorMatrix(
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                delta,
                0f,
                1f,
                0f,
                0f,
                delta,
                0f,
                0f,
                1f,
                0f,
                delta,
                0f,
                0f,
                0f,
                1f,
                0f,
            ),
        )

    private fun contrastMatrix(scale: Float): ColorMatrix {
        val translate = (-0.5f * scale + 0.5f) * 255f
        return ColorMatrix(
            floatArrayOf(
                scale,
                0f,
                0f,
                0f,
                translate,
                0f,
                scale,
                0f,
                0f,
                translate,
                0f,
                0f,
                scale,
                0f,
                translate,
                0f,
                0f,
                0f,
                1f,
                0f,
            ),
        )
    }

    /** Separable box blur over ARGB pixels. Deterministic; edges clamp to the nearest pixel. */
    private fun boxBlur(
        src: Bitmap,
        radius: Int,
    ): Bitmap {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        val tmp = IntArray(w * h)
        blurPass(pixels, tmp, w, h, radius, horizontal = true)
        blurPass(tmp, pixels, w, h, radius, horizontal = false)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    private fun blurPass(
        input: IntArray,
        output: IntArray,
        w: Int,
        h: Int,
        radius: Int,
        horizontal: Boolean,
    ) {
        val window = 2 * radius + 1
        for (line in 0 until (if (horizontal) h else w)) {
            val len = if (horizontal) w else h
            for (i in 0 until len) {
                var r = 0
                var g = 0
                var b = 0
                for (k in -radius..radius) {
                    val idx = (i + k).coerceIn(0, len - 1)
                    val px = if (horizontal) input[line * w + idx] else input[idx * w + line]
                    r += (px shr 16) and 0xFF
                    g += (px shr 8) and 0xFF
                    b += px and 0xFF
                }
                val color = (0xFF shl 24) or ((r / window) shl 16) or ((g / window) shl 8) or (b / window)
                if (horizontal) output[line * w + i] = color else output[i * w + line] = color
            }
        }
    }

    private const val BLUR_RADIUS = 4
    private const val CROP_ZOOM = 0.70f
    private const val BRIGHTNESS_DELTA = 45f
    private const val CONTRAST_UP = 1.35f
    private const val CONTRAST_DOWN = 0.70f
}
