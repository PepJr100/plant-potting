package com.darkfactory.plantpotting.permission

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Light-weight JVM Compose UI test for Bug 4 (PLANTPOTTING-0002 §1.2). Drives
 * [PermissionScreenHost] directly with a stub [CameraPermissionGuard] and a
 * hand-rolled [LifecycleOwner], so we can fire `ON_RESUME` without any Hilt
 * wiring or activity lifecycle gymnastics.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class PermissionScreenHostResumeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun resumeFromSettingsClearsPermanentlyDeniedAndFiresOnGrantedOnce() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = MutableGuard(context, initialGranted = false)
        val owner = ManualLifecycleOwner()
        var grantedCount = 0

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                PermissionScreenHost(
                    guard = guard,
                    onGranted = { grantedCount++ },
                    initialPermanentlyDenied = true,
                )
            }
        }

        // Initial state: permanently-denied UI; settings CTA visible.
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).assertIsDisplayed()
        assertThat(grantedCount).isEqualTo(0)

        // Simulate the Settings round-trip: user toggled the permission on.
        guard.granted = true

        // Compose has not yet re-read the guard — host should still show the
        // settings CTA until ON_RESUME fires.
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).assertIsDisplayed()

        // Fire the lifecycle resume event the host should be listening for.
        owner.handle(Lifecycle.Event.ON_START)
        owner.handle(Lifecycle.Event.ON_RESUME)
        composeRule.waitForIdle()

        // The permanently-denied UI must be gone, and onGranted must have
        // fired exactly once (idempotency guard for §1.3).
        val settingsNodes =
            composeRule
                .onAllNodesWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertThat(settingsNodes).isEmpty()
        assertThat(grantedCount).isEqualTo(1)

        // A second resume must not re-fire onGranted (the screen has already
        // navigated; a duplicate callback would push another camera route).
        owner.handle(Lifecycle.Event.ON_PAUSE)
        owner.handle(Lifecycle.Event.ON_RESUME)
        composeRule.waitForIdle()
        assertThat(grantedCount).isEqualTo(1)
    }

    @Test
    fun resumeWithoutGrantLeavesPermanentlyDeniedIntact() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = MutableGuard(context, initialGranted = false)
        val owner = ManualLifecycleOwner()
        var grantedCount = 0

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                PermissionScreenHost(
                    guard = guard,
                    onGranted = { grantedCount++ },
                    initialPermanentlyDenied = true,
                )
            }
        }

        owner.handle(Lifecycle.Event.ON_START)
        owner.handle(Lifecycle.Event.ON_RESUME)
        composeRule.waitForIdle()

        // Guard still reports ungranted → screen remains on the settings CTA.
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).assertIsDisplayed()
        assertThat(grantedCount).isEqualTo(0)
    }

    private class MutableGuard(
        context: Context,
        initialGranted: Boolean,
    ) : CameraPermissionGuard(context) {
        @Volatile var granted: Boolean = initialGranted

        override fun isGranted(): Boolean = granted

        override fun shouldShowRationale(activity: Activity): Boolean = false
    }

    private class ManualLifecycleOwner : LifecycleOwner {
        private val registry =
            LifecycleRegistry(this).also {
                it.currentState = Lifecycle.State.CREATED
            }

        override val lifecycle: Lifecycle = registry

        fun handle(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }
}
