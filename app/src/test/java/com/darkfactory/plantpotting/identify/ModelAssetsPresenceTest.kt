package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0003 §2.1 — assert the five on-device ML artefacts are bundled at the
 * documented asset path. Fails RED until §2.2/§2.3/§2.5 land them under
 * `app/src/main/assets/ml/aiy_plants_v1/`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ModelAssetsPresenceTest {
    private val dir = "ml/aiy_plants_v1"

    @Test
    fun mlDirectoryListsAllFiveRequiredArtefacts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entries =
            context.assets
                .list(dir)
                .orEmpty()
                .toSet()
        assertThat(entries).containsAtLeast(
            "model.tflite",
            "labels.csv",
            "plant_class_map.json",
            "model_manifest.json",
            "LICENSE-aiy-plants-v1.txt",
        )
    }

    @Test
    fun eachArtefactOpensFromAssetManager() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val artefacts =
            listOf(
                "model.tflite",
                "labels.csv",
                "plant_class_map.json",
                "model_manifest.json",
                "LICENSE-aiy-plants-v1.txt",
            )
        for (name in artefacts) {
            val stream = context.assets.open("$dir/$name")
            val firstByte = stream.read()
            stream.close()
            // -1 would mean an empty asset; any of these should have content.
            assertThat(firstByte).isNotEqualTo(-1)
        }
    }
}
