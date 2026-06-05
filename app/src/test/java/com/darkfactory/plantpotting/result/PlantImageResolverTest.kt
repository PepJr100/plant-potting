package com.darkfactory.plantpotting.result

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0010 Phase 6 — resolver falls back to the placeholder for any species without a
 * bundled CC0/PD image (currently every species), and never throws on an unknown id.
 */
class PlantImageResolverTest {
    @Test
    fun unknownSpeciesResolvesToPlaceholder() {
        assertThat(PlantImageResolver.drawableFor("definitely-not-a-species"))
            .isEqualTo(PlantImageResolver.placeholder)
        assertThat(PlantImageResolver.hasRealImage("definitely-not-a-species")).isFalse()
    }

    @Test
    fun blankSpeciesResolvesToPlaceholderWithoutCrashing() {
        assertThat(PlantImageResolver.drawableFor("")).isEqualTo(PlantImageResolver.placeholder)
    }
}
