package com.darkfactory.plantpotting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.darkfactory.plantpotting.camera.CameraScreenTags
import com.darkfactory.plantpotting.identify.FakeFixedIdentifier
import com.darkfactory.plantpotting.identify.OnDeviceIdentifyModule
import com.darkfactory.plantpotting.identify.PlantIdentifier
import com.darkfactory.plantpotting.identify.model.Candidate
import com.darkfactory.plantpotting.permission.FakeGuardStateRule
import com.darkfactory.plantpotting.result.LowConfidencePickerTags
import com.darkfactory.plantpotting.result.RecommendationScreenTags
import com.darkfactory.plantpotting.result.ResultScreenTags
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

/**
 * PLANTPOTTING-0005 §4.1–§4.3 — recovers B3 from PLANTPOTTING-0003 that never landed.
 *
 * The fake identifier reports `lowConfidence = true` AND seeds one mapped candidate
 * (Monstera deliciosa) via [FakeFixedIdentifier]'s `CandidateProvider` channel. The
 * camera view-model therefore routes to `NavCommand.LowConfidence` with a non-empty
 * candidate list, exercising the polished `LowConfidencePicker` as a primary
 * post-shutter destination — not a fallback.
 *
 * Two scenarios:
 *  - `lowConfidenceChipPathReachesRecommendation` — taps the seeded chip and confirms
 *    `ResultScreen` carries the `on-device match (low confidence)` source badge,
 *    then taps "See potting mix" to land on `RecommendationScreen`.
 *  - `lowConfidenceSearchPathReachesRecommendation` (§4.3) — drives the search field,
 *    taps a species row, and validates the same downstream path. Covers the path real
 *    users hit when the model emits zero mapped candidates.
 */
@HiltAndroidTest
@UninstallModules(OnDeviceIdentifyModule::class)
class LowConfidenceFlowTest {
    @BindValue @JvmField
    val fakeIdentifier: PlantIdentifier =
        FakeFixedIdentifier(
            speciesId = "monstera-deliciosa",
            displayName = "Swiss cheese plant",
            lowConfidence = true,
            seedCandidates =
                listOf(
                    Candidate(
                        speciesId = "monstera-deliciosa",
                        displayName = "Swiss cheese plant",
                        probability = 0.42f,
                    ),
                ),
        )

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val forceGranted = FakeGuardStateRule(granted = true)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun lowConfidenceChipPathReachesRecommendation() {
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewModelProbe.findCameraViewModel() != null
        }
        composeRule.runOnIdle {
            ViewModelProbe.findCameraViewModel()?.onCaptureReady(byteArrayOf(0, 1, 2, 3))
        }

        // LowConfidencePicker is the post-shutter destination, not a fallback.
        composeRule.onNodeWithTag(LowConfidencePickerTags.HEADLINE).assertIsDisplayed()
        composeRule.onNodeWithTag(LowConfidencePickerTags.SUBTITLE).assertIsDisplayed()
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.candidateTag("monstera-deliciosa"))
            .assertIsDisplayed()

        // Tap the seeded chip.
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.candidateTag("monstera-deliciosa"))
            .performClick()

        // ResultScreen: low-confidence source badge, user-confirmed pick.
        composeRule.onNodeWithTag(ResultScreenTags.SOURCE_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("On-device match (low confidence)").assertIsDisplayed()

        // RecommendationScreen reachable via See potting mix.
        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).performScrollTo().performClick()
        composeRule.onNodeWithTag(RecommendationScreenTags.ARCHETYPE_NAME).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun lowConfidenceSearchPathReachesRecommendation() {
        composeRule.startIdentifyFromHome()
        composeRule.onNodeWithTag(CameraScreenTags.SHUTTER).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            ViewModelProbe.findCameraViewModel() != null
        }
        composeRule.runOnIdle {
            ViewModelProbe.findCameraViewModel()?.onCaptureReady(byteArrayOf(0, 1, 2, 3))
        }

        composeRule.onNodeWithTag(LowConfidencePickerTags.SEARCH).assertIsDisplayed()
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.SEARCH)
            .performTextInput("monstera")
        composeRule
            .onNodeWithTag(LowConfidencePickerTags.speciesTag("monstera-deliciosa"))
            .performClick()

        composeRule.onNodeWithTag(ResultScreenTags.SOURCE_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("On-device match (low confidence)").assertIsDisplayed()

        composeRule.onNodeWithTag(ResultScreenTags.SEE_POTTING_MIX).performScrollTo().performClick()
        composeRule.onNodeWithTag(RecommendationScreenTags.ARCHETYPE_NAME).performScrollTo().assertIsDisplayed()
    }
}
