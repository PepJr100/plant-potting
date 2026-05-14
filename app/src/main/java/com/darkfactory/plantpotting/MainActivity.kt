package com.darkfactory.plantpotting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
                PlantPottingNavHost()
            }
        }
    }

    override fun onDestroy() {
        captureCacheCleaner.sweep()
        super.onDestroy()
    }
}
