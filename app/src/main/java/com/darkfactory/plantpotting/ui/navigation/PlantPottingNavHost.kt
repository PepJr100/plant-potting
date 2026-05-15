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
import com.darkfactory.plantpotting.camera.NavCommand
import com.darkfactory.plantpotting.di.AppEntryPoints
import com.darkfactory.plantpotting.identify.IdSource
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
                onNavigate = { command ->
                    when (command) {
                        is NavCommand.Success ->
                            navController.navigate(
                                Routes.result(
                                    speciesId = command.speciesId,
                                    source = command.source,
                                    lowConfidence = command.lowConfidence,
                                ),
                            )
                        is NavCommand.LowConfidence -> {
                            // PLANTPOTTING-0003 §5.8: the picker route lands in Phase 6.
                            // Until then, surface the user to a synthetic "not found" result
                            // path so they aren't stuck. Documented gap in the results doc.
                            navController.navigate(
                                Routes.result(
                                    speciesId = "",
                                    source = IdSource.ON_DEVICE_MODEL,
                                    lowConfidence = true,
                                ),
                            )
                        }
                        is NavCommand.Failure -> {
                            // Failure stays on the camera screen via CameraUiState.Failure;
                            // no navigation per §5.8.
                        }
                    }
                },
            )
        }
        composable(
            route = Routes.RESULT,
            arguments =
                listOf(
                    navArgument(Routes.ARG_SPECIES_ID) { type = NavType.StringType },
                    navArgument(Routes.ARG_SOURCE) {
                        type = NavType.StringType
                        defaultValue = IdSource.STUB_DETERMINISTIC.name
                    },
                    navArgument(Routes.ARG_LOW_CONFIDENCE) {
                        type = NavType.BoolType
                        defaultValue = false
                    },
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
