package com.darkfactory.plantpotting

import androidx.lifecycle.ViewModelStoreOwner
import com.darkfactory.plantpotting.camera.CameraViewModel

/**
 * Test-only helper. Reflectively walks the activity's view-model store
 * looking for a [CameraViewModel] instance so that instrumentation tests
 * can drive `onCaptureReady` without going through real CameraX hardware
 * (which is unreliable on Gradle Managed Devices).
 */
object ViewModelProbe {
    fun findCameraViewModel(owner: ViewModelStoreOwner): CameraViewModel? {
        // The Compose NavHost creates a per-destination ViewModelStore. The
        // activity-level store doesn't hold the CameraViewModel directly,
        // but the test can rely on the Hilt-provided graph to retrieve one.
        return try {
            val storeField = owner.viewModelStore.javaClass.getDeclaredField("map")
            storeField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val map = storeField.get(owner.viewModelStore) as Map<String, Any>
            map.values.firstOrNull { it is CameraViewModel } as? CameraViewModel
        } catch (_: Throwable) {
            null
        }
    }
}
