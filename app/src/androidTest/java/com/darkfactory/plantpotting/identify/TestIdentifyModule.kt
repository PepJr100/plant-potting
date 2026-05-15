package com.darkfactory.plantpotting.identify

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Instrumentation-only Hilt module that swaps the on-device model identifier with
 * [FakeFixedIdentifier]. PLANTPOTTING-0003 §4.5 — keeps `EndToEndFlowTest` and friends
 * deterministic without standing up a real TFLite interpreter against the placeholder
 * model bytes.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [OnDeviceIdentifyModule::class],
)
object TestIdentifyModule {
    @Provides
    @Singleton
    fun providePlantIdentifier(): PlantIdentifier = FakeFixedIdentifier()
}
