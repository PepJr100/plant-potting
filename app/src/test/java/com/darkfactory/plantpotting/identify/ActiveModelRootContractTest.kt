package com.darkfactory.plantpotting.identify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.BuildConfig
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelLabelsReader
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0007 §Phase 5 — locks the single `ACTIVE_MODEL_ROOT` switch.
 *
 * Two guarantees:
 *  1. **The committed default is the swap winner.** PLANTPOTTING-0007 shipped the House Plant
 *     Species MobileNetV2 as the production default (it beat AIY 6 high-conf to 1; see the results
 *     doc). This pins that decision — a silent revert to AIY (or to any other root) breaks here.
 *  2. **Root-threading derives the right asset paths.** The production providers
 *     (`OnDeviceIdentifyProvidersModule`) build the manifest/labels/mapping/model paths as
 *     `"$root/..."`. This test exercises that exact derivation against the default root and
 *     confirms it resolves the shipped candidate bundle at the expected layout.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ActiveModelRootContractTest {
    private val assets get() = ApplicationProvider.getApplicationContext<Context>().assets

    @Test
    fun committedDefaultRootIsTheSwapWinner() {
        assertThat(BuildConfig.ACTIVE_MODEL_ROOT).isEqualTo("ml/house_plant_species_mobilenetv2")
    }

    @Test
    fun providersDeriveManifestLabelsMappingFromRoot() {
        val root = BuildConfig.ACTIVE_MODEL_ROOT

        // Mirrors provideModelManifest(assets, root).
        val manifest = ModelManifestReader(assets, root).read()
        assertThat(manifest.labelCount).isEqualTo(47)

        // Mirrors provideModelLabels(assets, "$root/labels.csv").
        val labels = ModelLabelsReader(assets, "$root/labels.csv").read()
        assertThat(labels).hasSize(manifest.labelCount)

        // Mirrors provideModelLabelMap(assets, root). The candidate labels in common names.
        val labelMap = ModelLabelMapReader(assets, root).read()
        assertThat(labelMap.lookup("Snake plant (Sanseviera)")?.kbSpeciesId).isEqualTo("dracaena-trifasciata")
    }

    @Test
    fun derivedModelTflitePathExistsForDefaultRoot() {
        val root = BuildConfig.ACTIVE_MODEL_ROOT
        // Mirrors provideInterpreterFacade modelPath = "$root/model.tflite".
        val bytes = assets.open("$root/model.tflite").use { it.readBytes() }
        assertThat(bytes.size).isGreaterThan(0)
    }
}
