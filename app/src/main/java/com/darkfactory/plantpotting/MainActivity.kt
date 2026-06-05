package com.darkfactory.plantpotting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.darkfactory.plantpotting.camera.CaptureCacheCleaner
import com.darkfactory.plantpotting.persistence.PlantLogStore
import com.darkfactory.plantpotting.ui.navigation.PlantPottingNavHost
import com.darkfactory.plantpotting.ui.theme.PlantPottingTheme
import com.darkfactory.plantpotting.ui.theme.ThemeCandidate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var captureCacheCleaner: CaptureCacheCleaner

    @Inject lateinit var plantLogStore: PlantLogStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        captureCacheCleaner.sweep()
        setContent {
            // PLANTPOTTING-0010 D5 — apply the persisted (debug-selected) theme candidate; defaults
            // to LEAF. Production users never change it (switcher is BuildConfig.DEBUG-gated).
            val themeName by plantLogStore.themeCandidate.collectAsState(initial = ThemeCandidate.LEAF.name)
            PlantPottingTheme(candidate = ThemeCandidate.fromName(themeName)) {
                @OptIn(ExperimentalComposeUiApi::class)
                Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                    PlantPottingNavHost()
                }
            }
        }
    }

    override fun onDestroy() {
        captureCacheCleaner.sweep()
        super.onDestroy()
    }
}
