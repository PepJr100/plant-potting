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
    fun loadsBundledKbWithEightArchetypesAndSixteenSpecies() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val loader = KbLoader(assets = context.assets)
            val kb = loader.load()
            assertThat(kb.archetypes).hasSize(8)
            assertThat(kb.species).hasSize(16)
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
