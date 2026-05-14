package com.darkfactory.plantpotting.permission

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Sets [FakeCameraPermissionGuard]'s static overrides before the wrapped
 * statement (typically `createAndroidComposeRule`) launches the activity,
 * and restores prior state afterwards. Must be ordered with a lower @Rule
 * order than the compose rule so that it applies on the *outside* of it.
 */
class FakeGuardStateRule(
    private val granted: Boolean? = null,
    private val shouldShowRationale: Boolean? = null,
) : TestRule {
    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                val priorGranted = FakeCameraPermissionGuard.grantedOverride
                val priorRationale = FakeCameraPermissionGuard.shouldShowRationaleOverride
                FakeCameraPermissionGuard.grantedOverride = granted
                FakeCameraPermissionGuard.shouldShowRationaleOverride = shouldShowRationale
                try {
                    base.evaluate()
                } finally {
                    FakeCameraPermissionGuard.grantedOverride = priorGranted
                    FakeCameraPermissionGuard.shouldShowRationaleOverride = priorRationale
                }
            }
        }
}
