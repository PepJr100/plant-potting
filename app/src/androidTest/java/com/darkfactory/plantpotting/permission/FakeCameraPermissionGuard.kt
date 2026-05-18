package com.darkfactory.plantpotting.permission

import android.app.Activity
import android.content.Context

/**
 * Test-controllable [CameraPermissionGuard]. Tests flip the static
 * [grantedOverride] / [shouldShowRationaleOverride] / [permanentlyDeniedOverride]
 * *before* the activity launches (see `FakeGuardStateRule`) — using static
 * fields lets the rule mutate state from outside the Hilt instance graph.
 *
 * PLANTPOTTING-0005 §4.4 — [permanentlyDeniedOverride] lets the un-ignored
 * `openSettingsIntentFiresOnPermanentDenial` test drive the PermanentlyDenied
 * UI state without needing to drive the system permission dialog.
 */
class FakeCameraPermissionGuard(
    context: Context,
) : CameraPermissionGuard(context) {
    override fun isGranted(): Boolean = grantedOverride ?: super.isGranted()

    override fun shouldShowRationale(activity: Activity): Boolean = shouldShowRationaleOverride ?: super.shouldShowRationale(activity)

    override fun isPermanentlyDenied(): Boolean = permanentlyDeniedOverride ?: super.isPermanentlyDenied()

    companion object {
        @Volatile
        var grantedOverride: Boolean? = null

        @Volatile
        var shouldShowRationaleOverride: Boolean? = null

        @Volatile
        var permanentlyDeniedOverride: Boolean? = null

        fun reset() {
            grantedOverride = null
            shouldShowRationaleOverride = null
            permanentlyDeniedOverride = null
        }
    }
}
