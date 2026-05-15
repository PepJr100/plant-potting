package com.darkfactory.plantpotting.identify.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0004 §0.4 contract lock — input dtype is the manifest's editorial assertion
 * about the shipped model. RED today: `model_manifest.json` does not yet declare
 * `input_dtype`, and `ModelManifest` exposes no `inputDtype` field.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ModelManifestDtypeContractTest {
    private val dir = "ml/aiy_plants_v1"
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun manifestJsonDeclaresInputDtypeUint8() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val raw =
            context.assets
                .open("$dir/model_manifest.json")
                .use { it.readBytes() }
                .let { String(it, Charsets.UTF_8) }
        val obj = json.parseToJsonElement(raw).jsonObject
        val dtype = obj["input_dtype"]?.jsonPrimitive?.content
        assertThat(dtype).isEqualTo("uint8")
    }

    @Test
    fun parsedManifestExposesInputDtypeProperty() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manifest = ModelManifestReader(context.assets).read()
        assertThat(manifest.inputDtype).isEqualTo(ModelDtype.UINT8)
    }
}
