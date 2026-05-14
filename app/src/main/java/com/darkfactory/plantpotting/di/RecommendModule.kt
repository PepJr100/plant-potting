package com.darkfactory.plantpotting.di

import com.darkfactory.plantpotting.recommend.KbRecommendationEngine
import com.darkfactory.plantpotting.recommend.RecommendationEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecommendModule {
    @Binds
    @Singleton
    abstract fun bindRecommendationEngine(impl: KbRecommendationEngine): RecommendationEngine
}
