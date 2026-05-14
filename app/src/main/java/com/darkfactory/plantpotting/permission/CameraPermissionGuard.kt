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
 * Always re-reads permission state from the platform. Compose `remember`
 * after process death cannot be trusted, so callers must re-invoke
 * [isGranted] on each screen launch.
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
    }
