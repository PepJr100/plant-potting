package com.darkfactory.plantpotting.permission

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Sets [FakeCameraPermissionGuard]'s static overrides before the wrapped
 * statement (typically `createAndroidComposeRule`) launches the activity,
 * and restores prior state afterwards. Must be ordered with a lower @Rule
 * order than the compose rule so that it applies on the *outside* of it.
 *
 * PLANTPOTTING-0005 §4.4 — [permanentlyDenied] surfaces the new fake-guard
 * hook to drive `openSettingsIntentFiresOnPermanentDenial` without standing
 * up the system permission dialog.
 */
class FakeGuardStateRule(
    private val granted: Boolean? = null,
    private val shouldShowRationale: Boolean? = null,
    private val permanentlyDenied: Boolean? = null,
) : TestRule {
    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                val priorGranted = FakeCameraPermissionGuard.grantedOverride
                val priorRationale = FakeCameraPermissionGuard.shouldShowRationaleOverride
                val priorPermanentlyDenied = FakeCameraPermissionGuard.permanentlyDeniedOverride
                FakeCameraPermissionGuard.grantedOverride = granted
                FakeCameraPermissionGuard.shouldShowRationaleOverride = shouldShowRationale
                FakeCameraPermissionGuard.permanentlyDeniedOverride = permanentlyDenied
                try {
                    base.evaluate()
                } finally {
                    FakeCameraPermissionGuard.grantedOverride = priorGranted
                    FakeCameraPermissionGuard.shouldShowRationaleOverride = priorRationale
                    FakeCameraPermissionGuard.permanentlyDeniedOverride = priorPermanentlyDenied
                }
            }
        }
}
