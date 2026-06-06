package com.darkfactory.plantpotting

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.darkfactory.plantpotting.home.HomeTags

/**
 * PLANTPOTTING-0010 (review feedback) — the app now lands on the Home screen, so instrumented flows
 * that exercise the camera/permission subtree must first tap **Identify new plant**. With the fake
 * guard granted, this routes Home → Permission → (auto) Camera; ungranted, it stops on Permission.
 *
 * On a cold Gradle Managed Device start, MainActivity's `setContent` can attach a beat after the
 * ComposeTestRule begins driving the test. Reaching for the Identify tile in that window throws
 * `IllegalStateException: No compose hierarchies found in the app`. Wait for the tile to exist first
 * — `atLeastOneRootRequired = false` tolerates the no-hierarchy window instead of throwing.
 */
fun ComposeTestRule.startIdentifyFromHome() {
    waitUntil(timeoutMillis = 5_000) {
        onAllNodesWithTag(HomeTags.IDENTIFY)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
    onNodeWithTag(HomeTags.IDENTIFY).performClick()
}
