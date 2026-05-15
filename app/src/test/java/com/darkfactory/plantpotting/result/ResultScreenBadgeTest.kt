package com.darkfactory.plantpotting.result

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.identify.IdSource
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0003 §5.1 — locks the source-driven badge dispatch.
 *
 * Asserts that [badgeStringRes] returns the right string-resource id for each
 * (source, lowConfidence) tuple. Renders the actual badge string from resources for
 * each tuple to confirm the displayed text matches §5.1's table.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ResultScreenBadgeTest {
    private val context: Application = ApplicationProvider.getApplicationContext()

    private fun text(
        source: IdSource,
        lowConfidence: Boolean = false,
    ): String = context.getString(badgeStringRes(source, lowConfidence))

    @Test
    fun stubDeterministicMapsToLegacyStubCopy() {
        assertThat(text(IdSource.STUB_DETERMINISTIC)).isEqualTo("Stub identifier — replace in a later sprint")
        assertThat(text(IdSource.STUB_DETERMINISTIC, lowConfidence = true))
            .isEqualTo("Stub identifier — replace in a later sprint")
    }

    @Test
    fun stubRandomMapsToLegacyStubCopy() {
        assertThat(text(IdSource.STUB_RANDOM)).isEqualTo("Stub identifier — replace in a later sprint")
        assertThat(text(IdSource.STUB_RANDOM, lowConfidence = true))
            .isEqualTo("Stub identifier — replace in a later sprint")
    }

    @Test
    fun onDeviceHighConfidenceMapsToOnDeviceMatch() {
        assertThat(text(IdSource.ON_DEVICE_MODEL, lowConfidence = false)).isEqualTo("On-device match")
    }

    @Test
    fun onDeviceLowConfidenceMapsToOnDeviceMatchLowConfidence() {
        assertThat(text(IdSource.ON_DEVICE_MODEL, lowConfidence = true))
            .isEqualTo("On-device match (low confidence)")
    }

    @Test
    fun cloudSourceMapsToOfflineUnavailableCopy() {
        assertThat(text(IdSource.CLOUD)).isEqualTo("Cloud match unavailable in this offline build")
        assertThat(text(IdSource.CLOUD, lowConfidence = true))
            .isEqualTo("Cloud match unavailable in this offline build")
    }

    @Test
    fun fourNewStringResourcesExist() {
        // Belt-and-braces: ensure the resources the badge dispatcher relies on actually
        // resolve in the bundled strings.xml — a typo in one of the keys would otherwise
        // surface as a runtime crash rather than a compile error.
        assertThat(context.getString(R.string.result_badge_stub)).isNotEmpty()
        assertThat(context.getString(R.string.result_badge_on_device)).isNotEmpty()
        assertThat(context.getString(R.string.result_badge_on_device_low)).isNotEmpty()
        assertThat(context.getString(R.string.result_badge_cloud)).isNotEmpty()
    }
}
