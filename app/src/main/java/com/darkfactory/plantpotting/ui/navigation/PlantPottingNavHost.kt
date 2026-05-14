package com.darkfactory.plantpotting.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darkfactory.plantpotting.camera.CameraScreen
import com.darkfactory.plantpotting.di.AppEntryPoints
import com.darkfactory.plantpotting.permission.PermissionScreenHost
import com.darkfactory.plantpotting.result.RecommendationScreen
import com.darkfactory.plantpotting.result.ResultScreen
import dagger.hilt.android.EntryPointAccessors

@Composable
fun PlantPottingNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val guard =
        remember(context) {
            EntryPointAccessors
                .fromApplication(
                    context.applicationContext,
                    AppEntryPoints::class.java,
                ).cameraPermissionGuard()
        }

    NavHost(
        navController = navController,
        startDestination = Routes.PERMISSION,
    ) {
        composable(Routes.PERMISSION) {
            PermissionScreenHost(
                guard = guard,
                onGranted = {
                    navController.navigate(Routes.CAMERA) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CAMERA) {
            CameraScreen(
                viewModel = hiltViewModel(),
                onSpeciesIdentified = { speciesId ->
                    navController.navigate(Routes.result(speciesId))
                },
            )
        }
        composable(
            route = Routes.RESULT,
            arguments =
                listOf(
                    navArgument(Routes.ARG_SPECIES_ID) { type = NavType.StringType },
                ),
        ) {
            ResultScreen(
                viewModel = hiltViewModel(),
                onSeePottingMix = { speciesId ->
                    navController.navigate(Routes.recommendation(speciesId))
                },
            )
        }
        composable(
            route = Routes.RECOMMENDATION,
            arguments =
                listOf(
                    navArgument(Routes.ARG_SPECIES_ID) { type = NavType.StringType },
                ),
        ) {
            RecommendationScreen(
                viewModel = hiltViewModel(),
                onRetake = {
                    navController.navigate(Routes.CAMERA) {
                        popUpTo(Routes.CAMERA) { inclusive = true }
                    }
                },
            )
        }
    }
}
