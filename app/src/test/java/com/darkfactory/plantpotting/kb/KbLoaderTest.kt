package com.darkfactory.plantpotting.kb

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class KbLoaderTest {
    @Test
    fun loadsBundledKbWithNineArchetypesAndFortyFiveSpecies() =
        runBlocking {
            // PLANTPOTTING-0010: 44 (32 + 12 popular-slice delta). PLANTPOTTING-0012: +1 (pilea-peperomioides) = 45.
            val context = ApplicationProvider.getApplicationContext<Context>()
            val loader = KbLoader(assets = context.assets)
            val kb = loader.load()
            assertThat(kb.archetypes).hasSize(9)
            assertThat(kb.species).hasSize(45)
        }

    @Test
    fun bundledKbExposesSansevieriaAliasAndHoyaBlend() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val loader = KbLoader(assets = context.assets)
            val kb = loader.load()
            assertThat(kb.findSpecies("Sansevieria trifasciata")?.id)
                .isEqualTo("dracaena-trifasciata")
            assertThat(kb.findSpecies("Calathea orbifolia")?.id)
                .isEqualTo("goeppertia-orbifolia")
            val hoya = kb.species.single { it.scientificName == "Hoya carnosa" }
            assertThat(hoya.mapping)
                .isInstanceOf(
                    com.darkfactory.plantpotting.kb.model.ArchetypeMapping.Blend::class.java,
                )
        }
}
