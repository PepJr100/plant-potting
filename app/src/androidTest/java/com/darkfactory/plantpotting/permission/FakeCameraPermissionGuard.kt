package com.darkfactory.plantpotting.permission

import android.app.Activity
import android.content.Context

/**
 * Test-controllable [CameraPermissionGuard]. Tests flip the static
 * [grantedOverride] / [shouldShowRationaleOverride] *before* the activity
 * launches (see `FakeGuardStateRule`) — using static fields lets the rule
 * mutate state from outside the Hilt instance graph.
 */
class FakeCameraPermissionGuard(
    context: Context,
) : CameraPermissionGuard(context) {
    override fun isGranted(): Boolean = grantedOverride ?: super.isGranted()

    override fun shouldShowRationale(activity: Activity): Boolean = shouldShowRationaleOverride ?: super.shouldShowRationale(activity)

    companion object {
        @Volatile
        var grantedOverride: Boolean? = null

        @Volatile
        var shouldShowRationaleOverride: Boolean? = null

        fun reset() {
            grantedOverride = null
            shouldShowRationaleOverride = null
        }
    }
}
