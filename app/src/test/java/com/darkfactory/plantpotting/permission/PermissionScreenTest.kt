package com.darkfactory.plantpotting.permission

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class PermissionScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun rationaleStateShowsRationaleAndGrantButton() {
        composeRule.setContent {
            PermissionScreen(
                state = PermissionUiState.NotYetAsked,
                onGrantClick = {},
                onOpenSettingsClick = {},
            )
        }
        composeRule.onNodeWithTag(PermissionScreenTags.RATIONALE).assertIsDisplayed()
        composeRule.onNodeWithTag(PermissionScreenTags.GRANT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun grantClickFiresCallback() {
        var fired = false
        composeRule.setContent {
            PermissionScreen(
                state = PermissionUiState.NotYetAsked,
                onGrantClick = { fired = true },
                onOpenSettingsClick = {},
            )
        }
        composeRule.onNodeWithTag(PermissionScreenTags.GRANT_BUTTON).performClick()
        assertThat(fired).isTrue()
    }

    @Test
    fun permanentDenialShowsOpenSettings() {
        var fired = false
        composeRule.setContent {
            PermissionScreen(
                state = PermissionUiState.PermanentlyDenied,
                onGrantClick = {},
                onOpenSettingsClick = { fired = true },
            )
        }
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(PermissionScreenTags.OPEN_SETTINGS_BUTTON).performClick()
        assertThat(fired).isTrue()
    }

    @Test
    fun whyWeAskTogglesBody() {
        composeRule.setContent {
            PermissionScreen(
                state = PermissionUiState.NotYetAsked,
                onGrantClick = {},
                onOpenSettingsClick = {},
            )
        }
        composeRule.onNodeWithTag(PermissionScreenTags.WHY_TOGGLE).performClick()
        composeRule.onNodeWithTag(PermissionScreenTags.WHY_BODY).assertIsDisplayed()
    }
}
