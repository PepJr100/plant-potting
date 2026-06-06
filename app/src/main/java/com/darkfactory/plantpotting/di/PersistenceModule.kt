package com.darkfactory.plantpotting.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.darkfactory.plantpotting.persistence.DataStorePlantLogStore
import com.darkfactory.plantpotting.persistence.PlantLogDocument
import com.darkfactory.plantpotting.persistence.PlantLogSerializer
import com.darkfactory.plantpotting.persistence.PlantLogStore
import com.darkfactory.plantpotting.persistence.SystemTimeProvider
import com.darkfactory.plantpotting.persistence.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Local on-device persistence wiring (PLANTPOTTING-0010 D1). One typed [DataStore] document,
 * one [PlantLogStore] singleton over it. Local-file only — nothing here touches the network.
 */
private val Context.plantLogDataStore: DataStore<PlantLogDocument> by dataStore(
    fileName = "plant_log.json",
    serializer = PlantLogSerializer,
)

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    @Singleton
    fun providePlantLogDataStore(
        @ApplicationContext context: Context,
    ): DataStore<PlantLogDocument> = context.plantLogDataStore

    @Provides
    @Singleton
    fun provideTimeProvider(impl: SystemTimeProvider): TimeProvider = impl

    @Provides
    @Singleton
    fun providePlantLogStore(
        dataStore: DataStore<PlantLogDocument>,
        timeProvider: TimeProvider,
    ): PlantLogStore = DataStorePlantLogStore(dataStore, timeProvider)
}
