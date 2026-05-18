package com.darkfactory.plantpotting.camera

import androidx.camera.core.ImageCapture
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.MainActivity
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * PLANTPOTTING-0002 Phase 4 (UX 1). The shutter must be disabled and a
 * `BIND_PROGRESS` overlay must be visible while CameraX hasn't yet returned
 * an `ImageCapture`. Once an `ImageCapture` is bound, the overlay must
 * disappear and the shutter must become enabled.
 *
 * AOSP system images for the `pixel6Api34` GMD ship with an emulated back
 * camera, so the real `bindCameraUseCases` succeeds at runtime and leaves
 * `imageCapture` non-null — the bind-pending state would otherwise be
 * unobservable from instrumentation. Tests therefore drive
 * [CameraScreenTestRegistry.forceSkipBind] via a JUnit rule that takes
 * effect BEFORE `composeRule`'s activity launches.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class CameraScreenBindStateTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 4)
    val registrySetup = CameraScreenRegistrySetupRule(forceSkipBind = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @After
    fun clearRegistry() {
        CameraScreenTestRegistry.testImageCapture = null
        CameraScreenTestRegistry.forceSkipBind = false
    }

    @Test
    fun shutterIsDisabledAndBindProgressVisibleWhileImageCaptureIsNull() {
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsNotEnabled()
        composeRule.onNodeWithTag(CameraScreenTags.BIND_PROGRESS).assertIsDisplayed()
    }
}

/**
 * Paired bound-state test in a separate class so the @Rule chain can
 * inject the `ImageCapture` BEFORE the activity launches. Splitting from
 * [CameraScreenBindStateTest] avoids per-test rule reconfiguration.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class CameraScreenBoundStateTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 4)
    val registrySetup =
        CameraScreenRegistrySetupRule(
            testImageCaptureFactory = { ImageCapture.Builder().build() },
        )

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @After
    fun clearRegistry() {
        CameraScreenTestRegistry.testImageCapture = null
        CameraScreenTestRegistry.forceSkipBind = false
    }

    @Test
    fun shutterIsEnabledAndBindProgressGoneWhenImageCaptureIsBound() {
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsEnabled()
        val bindProgress =
            composeRule
                .onAllNodesWithTag(CameraScreenTags.BIND_PROGRESS)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
        check(bindProgress.isEmpty()) { "BIND_PROGRESS overlay rendered while ImageCapture was bound" }
    }
}

/**
 * Sets [CameraScreenTestRegistry] state BEFORE the wrapped statement runs
 * (i.e., before `createAndroidComposeRule` launches the activity), and
 * restores on completion. Must be ordered with a HIGHER `@Rule(order=...)`
 * than the compose rule so it applies on the outside.
 */
class CameraScreenRegistrySetupRule(
    private val testImageCaptureFactory: (() -> ImageCapture)? = null,
    private val forceSkipBind: Boolean = false,
) : TestRule {
    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                val priorImageCapture = CameraScreenTestRegistry.testImageCapture
                val priorSkip = CameraScreenTestRegistry.forceSkipBind
                CameraScreenTestRegistry.testImageCapture = testImageCaptureFactory?.invoke()
                CameraScreenTestRegistry.forceSkipBind = forceSkipBind
                try {
                    base.evaluate()
                } finally {
                    CameraScreenTestRegistry.testImageCapture = priorImageCapture
                    CameraScreenTestRegistry.forceSkipBind = priorSkip
                }
            }
        }
}
