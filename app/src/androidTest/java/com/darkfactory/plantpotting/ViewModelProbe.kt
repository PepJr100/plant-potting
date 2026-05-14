package com.darkfactory.plantpotting

import com.darkfactory.plantpotting.camera.CameraScreenTestRegistry
import com.darkfactory.plantpotting.camera.CameraViewModel

/**
 * Test-only helper. Returns the currently-composed [CameraViewModel]. The
 * activity's `ViewModelStore` does *not* hold this view model — it lives in
 * the Compose Navigation back-stack-entry's store — so [CameraScreen]
 * registers itself with [CameraScreenTestRegistry] during composition.
 *
 * Callers must first wait for the camera screen to compose (e.g.
 * `composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()`)
 * before reading the value.
 */
object ViewModelProbe {
    fun findCameraViewModel(): CameraViewModel? = CameraScreenTestRegistry.current
}
