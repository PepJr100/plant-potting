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
 *  1. **R8 — the committed default stays AIY.** A prototype build/branch flips
 *     `BuildConfig.ACTIVE_MODEL_ROOT` to the winning model's root, but `main` must never ship
 *     it flipped (that would silently swap the production model without the comparison gate).
 *  2. **Root-threading derives the right asset paths.** The production providers
 *     (`OnDeviceIdentifyProvidersModule`) build the manifest/labels/mapping/model paths as
 *     `"$root/..."`. This test exercises that exact derivation against the default root and
 *     confirms it resolves the real AIY bundle — so a future flip to a candidate root only
 *     needs that bundle present at the same layout.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ActiveModelRootContractTest {
    private val assets get() = ApplicationProvider.getApplicationContext<Context>().assets

    @Test
    fun committedDefaultRootIsTheFrozenAiyBaseline() {
        assertThat(BuildConfig.ACTIVE_MODEL_ROOT).isEqualTo("ml/aiy_plants_v1")
    }

    @Test
    fun providersDeriveManifestLabelsMappingFromRoot() {
        val root = BuildConfig.ACTIVE_MODEL_ROOT

        // Mirrors provideModelManifest(assets, root).
        val manifest = ModelManifestReader(assets, root).read()
        assertThat(manifest.labelCount).isEqualTo(2102)

        // Mirrors provideModelLabels(assets, "$root/labels.csv").
        val labels = ModelLabelsReader(assets, "$root/labels.csv").read()
        assertThat(labels).hasSize(manifest.labelCount)

        // Mirrors provideModelLabelMap(assets, root).
        val labelMap = ModelLabelMapReader(assets, root).read()
        assertThat(labelMap.lookup("Monstera deliciosa")?.kbSpeciesId).isEqualTo("monstera-deliciosa")
    }

    @Test
    fun derivedModelTflitePathExistsForDefaultRoot() {
        val root = BuildConfig.ACTIVE_MODEL_ROOT
        // Mirrors provideInterpreterFacade modelPath = "$root/model.tflite".
        val bytes = assets.open("$root/model.tflite").use { it.readBytes() }
        assertThat(bytes.size).isGreaterThan(0)
    }
}
