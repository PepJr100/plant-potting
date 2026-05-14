package com.darkfactory.plantpotting.di

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    @Provides
    @Singleton
    fun provideCameraProviderFuture(
        @ApplicationContext context: Context,
    ): com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)
}
