package com.darkfactory.plantpotting.permission

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces the production [PermissionModule] so that instrumentation tests can
 * force the perceived permission state without invoking `pm revoke`, which
 * would kill the test process. Tests reach into [FakeCameraPermissionGuard]
 * via `EntryPointAccessors` (or by `@Inject`-ing it) to flip
 * [FakeCameraPermissionGuard.grantedOverride] before launching the activity.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PermissionModule::class],
)
object TestPermissionModule {
    @Provides
    @Singleton
    fun provideCameraPermissionGuard(
        @ApplicationContext context: Context,
    ): CameraPermissionGuard = FakeCameraPermissionGuard(context)
}
