package com.darkfactory.plantpotting.permission

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {
    @Provides
    @Singleton
    fun provideCameraPermissionGuard(
        @ApplicationContext context: Context,
    ): CameraPermissionGuard = CameraPermissionGuard(context)
}
