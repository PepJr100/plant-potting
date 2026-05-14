package com.darkfactory.plantpotting.identify

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IdentifyModule {

    @Binds
    @Singleton
    abstract fun bindPlantIdentifier(impl: StubPlantIdentifier): PlantIdentifier
}
