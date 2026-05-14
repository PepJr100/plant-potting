package com.darkfactory.plantpotting.di

import com.darkfactory.plantpotting.permission.CameraPermissionGuard
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt-managed accessors for components that need to be obtained from a
 * non-injection-aware site (e.g. composables that don't have a ViewModel).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoints {
    fun cameraPermissionGuard(): CameraPermissionGuard
}
