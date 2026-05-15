package com.darkfactory.plantpotting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.darkfactory.plantpotting.camera.CaptureCacheCleaner
import com.darkfactory.plantpotting.ui.navigation.PlantPottingNavHost
import com.darkfactory.plantpotting.ui.theme.PlantPottingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var captureCacheCleaner: CaptureCacheCleaner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        captureCacheCleaner.sweep()
        setContent {
            PlantPottingTheme {
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
