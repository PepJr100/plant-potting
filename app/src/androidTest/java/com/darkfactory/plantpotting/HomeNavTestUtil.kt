package com.darkfactory.plantpotting

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.home.HomeTags

/**
 * PLANTPOTTING-0010 (review feedback) — the app now lands on the Home screen, so instrumented flows
 * that exercise the camera/permission subtree must first tap **Identify new plant**. With the fake
 * guard granted, this routes Home → Permission → (auto) Camera; ungranted, it stops on Permission.
 */
fun ComposeTestRule.startIdentifyFromHome() {
    onNodeWithTag(HomeTags.IDENTIFY).performClick()
}
