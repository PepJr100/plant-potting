package com.darkfactory.plantpotting.identify

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [IdentifyModule::class],
)
object TestIdentifyModule {
    @Provides
    @Singleton
    fun providePlantIdentifier(): PlantIdentifier = FakeFixedIdentifier()
}
