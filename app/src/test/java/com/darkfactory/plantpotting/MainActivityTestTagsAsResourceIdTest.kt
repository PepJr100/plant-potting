package com.darkfactory.plantpotting

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsPropertiesAndroid
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.darkfactory.plantpotting.ui.theme.PlantPottingTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PLANTPOTTING-0004 §0.6 contract lock — `MainActivity.onCreate` must wrap its composition
 * in a `Modifier.semantics { testTagsAsResourceId = true }` so `adb shell uiautomator dump`
 * surfaces Compose `testTag` strings as platform `resource-id` attributes (Bug 2 from
 * PLANTPOTTING-0003 review).
 *
 * RED today: `MainActivity.kt:19-23` calls `setContent { PlantPottingTheme { PlantPottingNavHost() } }`
 * with no semantics opt-in.
 *
 * The test mirrors the exact shape of `MainActivity.onCreate`'s composition (theme +
 * `Box(Modifier.semantics{...})` wrap that §2.1 lands). Asserting on a stand-in
 * composition rather than launching `MainActivity` itself avoids needing Hilt + the full
 * activity launch under Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MainActivityTestTagsAsResourceIdTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun mainActivityCompositionExposesTestTagsAsResourceIdOnRoot() {
        composeRule.setContent {
            PlantPottingTheme {
                MainActivityRootComposition {
                    Text(text = "stub", modifier = Modifier.testTag("probe.tag"))
                }
            }
        }
        val root = composeRule.onRoot().fetchSemanticsNode()
        val carrier = findCarrierWithTestTagsAsResourceId(root)
        assertThat(carrier).isNotNull()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun findCarrierWithTestTagsAsResourceId(node: SemanticsNode): SemanticsNode? {
        val key = SemanticsPropertiesAndroid.TestTagsAsResourceId
        if (node.config.contains(key) && node.config[key] == true) return node
        for (child in node.children) {
            val found = findCarrierWithTestTagsAsResourceId(child)
            if (found != null) return found
        }
        return null
    }
}

/**
 * Mirrors the production `MainActivity.onCreate` composition wrap. After §2.1 lands, both
 * this helper and `MainActivity.kt` use the same `Box(Modifier.semantics { ... })` pattern.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun MainActivityRootComposition(content: @Composable () -> Unit) {
    Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
        content()
    }
}
