package com.darkfactory.plantpotting.result

import androidx.annotation.DrawableRes
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 Phase 6 / D4 — resolves a KB species id to a bundled **CC0/PD** reference image
 * (downscaled WebP under `res/drawable-nodpi/`), falling back to an authored placeholder vector when
 * no clean image is bundled.
 *
 * The [images] registry starts empty: sprint 0008 found license-clean houseplant imagery is scarce
 * (~1–6.8%), so we ship the infrastructure + placeholder and add verified images incrementally. Every
 * entry added here MUST have a matching row in `docs/licenses/reference-images.md` (CI-cross-checked
 * by `ReferenceImageManifestTest`), and the file must live in `res/drawable-nodpi/`.
 */
object PlantImageResolver {
    @DrawableRes
    val placeholder: Int = R.drawable.ic_plant_placeholder

    /** speciesId → bundled CC0/PD WebP drawable. Empty until verified images land. */
    private val images: Map<String, Int> = emptyMap()

    /** The drawable to render for [speciesId] — a real reference image if bundled, else the placeholder. */
    @DrawableRes
    fun drawableFor(speciesId: String): Int = images[speciesId] ?: placeholder

    /** True iff a real (non-placeholder) reference image is bundled for [speciesId]. */
    fun hasRealImage(speciesId: String): Boolean = images.containsKey(speciesId)
}
