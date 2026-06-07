package com.darkfactory.plantpotting.identify

import android.content.res.AssetManager
import com.darkfactory.plantpotting.BuildConfig
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

/**
 * PLANTPOTTING-0007 §Phase 5 — the one model-root switch, isolated in its own module so an
 * instrumentation test can `@UninstallModules(ActiveModelRootModule::class)` + `@BindValue` a
 * different root (e.g. the prototype candidate) without disturbing any other provider.
 * Production reads `BuildConfig.ACTIVE_MODEL_ROOT` (default `ml/aiy_plants_v1`, flipped to the
 * winning model's root on the prototype build/branch).
 */
@Module
@InstallIn(SingletonComponent::class)
object ActiveModelRootModule {
    @Provides
    @Singleton
    @ActiveModelRoot
    fun provideActiveModelRoot(): String = BuildConfig.ACTIVE_MODEL_ROOT
}

@Module
@InstallIn(SingletonComponent::class)
object OnDeviceIdentifyProvidersModule {
    @Provides
    @Singleton
    fun provideModelManifest(
        assets: AssetManager,
        @ActiveModelRoot root: String,
    ): ModelManifest = ModelManifestReader(assets, root).read()

    @Provides
    @Singleton
    @ModelLabelsList
    fun provideModelLabels(
        assets: AssetManager,
        @ActiveModelRoot root: String,
    ): List<String> = ModelLabelsReader(assets, "$root/labels.csv").read()

    @Provides
    @Singleton
    fun provideModelLabelMap(
        assets: AssetManager,
        @ActiveModelRoot root: String,
    ): ModelLabelMap = ModelLabelMapReader(assets, root).read()

    @Provides
    @Singleton
    fun provideThresholds(manifest: ModelManifest): ModelManifest.Thresholds = manifest.thresholds

    @Provides
    @Singleton
    @PerSpeciesThresholds
    fun providePerSpeciesThresholds(manifest: ModelManifest): Map<String, Float> = manifest.perSpeciesThresholds

    // PLANTPOTTING-0012 — pothos↔Pilea boundary gate config (distinct List type; no qualifier needed).
    @Provides
    @Singleton
    fun provideBoundaryPairs(manifest: ModelManifest): List<ModelManifest.BoundaryPair> = manifest.boundaryPairs

    @Provides
    @Singleton
    fun provideInterpreterFacade(
        assets: AssetManager,
        manifest: ModelManifest,
        @ActiveModelRoot root: String,
    ): InterpreterFacade =
        TfLiteInterpreterFacade(
            assets = assets,
            modelPath = "$root/model.tflite",
            labelCount = manifest.labelCount,
            inputSize = manifest.inputSize,
            expectedInputDtype = manifest.inputDtype,
        )

    @Provides
    @Singleton
    @InferenceDispatcher
    fun provideInferenceDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
