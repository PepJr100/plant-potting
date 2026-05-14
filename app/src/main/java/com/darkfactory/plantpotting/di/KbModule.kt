package com.darkfactory.plantpotting.di

import android.content.Context
import android.content.res.AssetManager
import com.darkfactory.plantpotting.kb.KbLoader
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
object KbModule {

    @Provides
    @Singleton
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager = context.assets

    @Provides
    @Singleton
    fun provideKnowledgeBase(loader: KbLoader): KnowledgeBase = runBlocking { loader.load() }
}
