package com.darkfactory.plantpotting.ui.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darkfactory.plantpotting.R
import com.darkfactory.plantpotting.camera.CameraScreen
import com.darkfactory.plantpotting.camera.NavCommand
import com.darkfactory.plantpotting.di.AppEntryPoints
import com.darkfactory.plantpotting.home.HomeScreen
import com.darkfactory.plantpotting.home.HomeViewModel
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.permission.PermissionScreenHost
import com.darkfactory.plantpotting.ui.theme.DebugThemeSwitcherScreen
import com.darkfactory.plantpotting.result.AddThisPlantScreen
import com.darkfactory.plantpotting.result.ArchetypePickerScreen
import com.darkfactory.plantpotting.result.LowConfidencePickerScreen
import com.darkfactory.plantpotting.result.MyPlantsScreen
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
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val recent by homeViewModel.recentPlants.collectAsState()
            var showAbout by remember { mutableStateOf(false) }
            HomeScreen(
                recentPlants = recent,
                onIdentify = { navController.navigate(Routes.PERMISSION) },
                onMyPlants = { navController.navigate(Routes.MY_PLANTS) },
                onBrowseMixes = { navController.navigate(Routes.ARCHETYPE_PICKER) },
                onAbout = { showAbout = true },
                onRecentClick = { row ->
                    navController.navigate(
                        Routes.result(
                            speciesId = row.speciesId,
                            source = row.source,
                            lowConfidence = false,
                            confidencePct = row.confidencePct,
                        ),
                    )
                },
            )
            if (showAbout) {
                AlertDialog(
                    onDismissRequest = { showAbout = false },
                    confirmButton = {
                        TextButton(onClick = { showAbout = false }) {
                            Text(stringResource(id = R.string.home_about_dismiss))
                        }
                    },
                    title = { Text(stringResource(id = R.string.home_about_title)) },
                    text = { Text(stringResource(id = R.string.home_about_body)) },
                )
            }
        }
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
                onOpenMyPlants = { navController.navigate(Routes.MY_PLANTS) },
                onOpenThemeSwitcher = { navController.navigate(Routes.THEME_SWITCHER) },
                onNavigate = { command ->
                    when (command) {
                        is NavCommand.Success ->
                            navController.navigate(
                                Routes.result(
                                    speciesId = command.speciesId,
                                    source = command.source,
                                    lowConfidence = command.lowConfidence,
                                    confidencePct = command.confidencePct,
                                ),
                            )
                        is NavCommand.LowConfidence ->
                            navController.navigate(Routes.lowConfidencePicker(command.candidates))
                        is NavCommand.AddPlant ->
                            navController.navigate(
                                Routes.addThisPlant(command.modelClassLabel, command.confidencePct),
                            )
                        is NavCommand.Failure -> {
                            // Failure stays on the camera screen via CameraUiState.Failure;
                            // no navigation per PLANTPOTTING-0003 §5.8.
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
                    navArgument(Routes.ARG_CONFIDENCE_PCT) {
                        type = NavType.IntType
                        defaultValue = Routes.CONFIDENCE_ABSENT
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
        composable(
            route = Routes.LOW_CONFIDENCE_PICKER,
            arguments =
                listOf(
                    navArgument(Routes.ARG_CANDIDATES) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
        ) {
            LowConfidencePickerScreen(
                viewModel = hiltViewModel(),
                onSpeciesPicked = { speciesId ->
                    navController.navigate(
                        Routes.result(
                            speciesId = speciesId,
                            source = IdSource.ON_DEVICE_MODEL,
                            lowConfidence = true,
                        ),
                    )
                },
                onPickByArchetype = {
                    navController.navigate(Routes.ARCHETYPE_PICKER)
                },
            )
        }
        composable(Routes.MY_PLANTS) {
            MyPlantsScreen(
                viewModel = hiltViewModel(),
                onPlantClick = { row ->
                    navController.navigate(
                        Routes.result(
                            speciesId = row.speciesId,
                            source = row.source,
                            lowConfidence = false,
                            confidencePct = row.confidencePct,
                        ),
                    )
                },
            )
        }
        composable(
            route = Routes.ADD_THIS_PLANT,
            arguments =
                listOf(
                    navArgument(Routes.ARG_MODEL_CLASS_LABEL) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Routes.ARG_CONFIDENCE_PCT) {
                        type = NavType.IntType
                        defaultValue = Routes.CONFIDENCE_ABSENT
                    },
                ),
        ) {
            AddThisPlantScreen(
                viewModel = hiltViewModel(),
                onPickManually = {
                    // Fall through to the existing manual picker (no mapped candidates to seed).
                    navController.navigate(Routes.lowConfidencePicker(emptyList()))
                },
            )
        }
        composable(Routes.THEME_SWITCHER) {
            DebugThemeSwitcherScreen(viewModel = hiltViewModel())
        }
        composable(Routes.ARCHETYPE_PICKER) {
            ArchetypePickerScreen(
                viewModel = hiltViewModel(),
                onArchetypePicked = { archetypeId ->
                    navController.navigate(Routes.archetypeRecommendation(archetypeId))
                },
            )
        }
        composable(
            route = Routes.ARCHETYPE_RECOMMENDATION,
            arguments =
                listOf(
                    navArgument(Routes.ARG_ARCHETYPE_ID) { type = NavType.StringType },
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
