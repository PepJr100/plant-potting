package com.darkfactory.plantpotting.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Always re-reads permission state from the platform. The companion
 * [com.darkfactory.plantpotting.permission.PermissionScreenHost] caches the
 * result in Compose state and re-invokes [isGranted] on every `ON_RESUME`
 * so a Settings round-trip is observed without a process restart (Bug 4 /
 * PLANTPOTTING-0002 §1).
 */
@Singleton
open class CameraPermissionGuard
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        open fun isGranted(): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        open fun shouldShowRationale(activity: Activity): Boolean =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)

        /**
         * Whether the user has previously selected "don't ask again". The platform has no
         * direct query for this — production callers (`PermissionScreenHost`) infer it from
         * the launcher's denial callback + `!shouldShowRationale`. This hook is provided so
         * instrumentation tests can drive the permanent-denied state without going through
         * the system permission dialog (`PermissionDeniedFlowTest` /
         * `FakeCameraPermissionGuard`). The default of `false` preserves production
         * behaviour — the host's existing launcher-based tracking is unaffected.
         */
        open fun isPermanentlyDenied(): Boolean = false
    }
