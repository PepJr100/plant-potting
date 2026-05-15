package com.darkfactory.plantpotting.identify

import android.content.res.AssetManager
import com.darkfactory.plantpotting.identify.model.InterpreterFacade
import com.darkfactory.plantpotting.identify.model.ModelLabelMap
import com.darkfactory.plantpotting.identify.model.ModelLabelMapReader
import com.darkfactory.plantpotting.identify.model.ModelLabelsReader
import com.darkfactory.plantpotting.identify.model.ModelManifest
import com.darkfactory.plantpotting.identify.model.ModelManifestReader
import com.darkfactory.plantpotting.identify.model.TfLiteInterpreterFacade
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Production Hilt wiring for the on-device classifier (PLANTPOTTING-0003 §4.1 / §4.6).
 *
 * Splits responsibilities:
 *  - [OnDeviceIdentifyModule] (`abstract class`) hosts the single `@Binds` that points
 *    `PlantIdentifier` at `OnDevicePlantIdentifier`. This is what instrumentation tests
 *    `@TestInstallIn(replaces = …)` to swap in a fake.
 *  - The nested `Providers` object hosts the assets/manifest/labels/mapping/threshold/
 *    interpreter/dispatcher providers — these are read from the AssetManager at app
 *    boot and shared as singletons across `identify(jpeg)` calls.
 *
 * The split lets a test swap the binding without re-providing every collaborator.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OnDeviceIdentifyModule {
    @Binds
    @Singleton
    abstract fun bindPlantIdentifier(impl: OnDevicePlantIdentifier): PlantIdentifier
}

@Module
@InstallIn(SingletonComponent::class)
object OnDeviceIdentifyProvidersModule {
    @Provides
    @Singleton
    fun provideModelManifest(assets: AssetManager): ModelManifest = ModelManifestReader(assets).read()

    @Provides
    @Singleton
    @ModelLabelsList
    fun provideModelLabels(assets: AssetManager): List<String> = ModelLabelsReader(assets).read()

    @Provides
    @Singleton
    fun provideModelLabelMap(assets: AssetManager): ModelLabelMap = ModelLabelMapReader(assets).read()

    @Provides
    @Singleton
    fun provideThresholds(manifest: ModelManifest): ModelManifest.Thresholds = manifest.thresholds

    @Provides
    @Singleton
    fun provideInterpreterFacade(
        assets: AssetManager,
        manifest: ModelManifest,
    ): InterpreterFacade =
        TfLiteInterpreterFacade(
            assets = assets,
            modelPath = "ml/aiy_plants_v1/model.tflite",
            labelCount = manifest.labelCount,
            inputSize = manifest.inputSize,
            expectedInputDtype = manifest.inputDtype,
        )

    @Provides
    @Singleton
    @InferenceDispatcher
    fun provideInferenceDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
