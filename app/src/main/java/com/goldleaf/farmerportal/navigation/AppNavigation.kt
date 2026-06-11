package com.goldleaf.farmerportal.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.goldleaf.certification.navigation.CertificationRoutes
import com.goldleaf.core.auth.UserSessionManager

// Import all your screens
import com.goldleaf.farmerportal.ui.splash.SplashScreen
import com.goldleaf.feature.farmermanagement.ui.dashboard.DashboardScreen
import com.goldleaf.feature.farmermanagement.ui.ProfileScreen
import com.goldleaf.feature.farmermanagement.ui.FarmSetupScreen
import com.goldleaf.feature.farmermanagement.ui.screens.FarmFencingScreen
import com.goldleaf.feature.farmermanagement.ui.screens.FarmerManagementScreen
import com.goldleaf.feature.cropmanagement.ui.selection.CropSelectionScreen
import com.goldleaf.feature.cropmanagement.ui.details.CropDetailsScreen
import com.goldleaf.feature.weatherclimate.ui.WeatherScreen
import com.goldleaf.feature.advisoryservices.ui.AdvisoryDashboardScreen
import com.goldleaf.feature.trainingextension.ui.TrainingScreen
import com.goldleaf.feature.advisoryservices.ui.DiseaseDetectionScreen
import com.goldleaf.feature.advisoryservices.ui.PestDetectionScreen
import com.goldleaf.feature.advisoryservices.ui.SoilAnalysisScreen
import com.goldleaf.feature.cropmanagement.ui.activity.AddCropTasksScreen
import com.goldleaf.feature.cropmanagement.ui.input.InputTrackingScreen
import com.goldleaf.feature.cropmanagement.ui.seasonal.SeasonalPlanningScreen
import com.goldleaf.feature.cropmanagement.ui.compliance.ComplianceTrackingScreen
import com.goldleaf.feature.cropmanagement.ui.soil.SoilProfileScreen
import com.goldleaf.feature.cropmanagement.ui.plots.PlotManagementScreen
import com.goldleaf.feature.farmermanagement.ui.revenue.RevenueScreen
import com.goldleaf.feature.farmermanagement.ui.ChangePasswordScreen
import com.goldleaf.feature.trainingextension.ui.TrainingDetailsScreen
import com.goldleaf.feature.trainingextension.ui.VideoPlayerScreen
// Auth & Certification NavGraphs
import com.goldleaf.feature.farmermanagement.navigation.authNavGraph
import com.goldleaf.feature.farmermanagement.navigation.AuthRoutes
import com.goldleaf.certification.navigation.certificationNavGraph
import com.goldleaf.certification.navigation.qualityNavGraph
import com.goldleaf.certification.navigation.productAuthNavGraph
import com.goldleaf.feature.farmermanagement.ui.screens.FarmSelectionScreen
import com.goldleaf.feature.farmermanagement.ui.screens.MyCropsScreen
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard/{farmId}"
    const val MY_CROPS = "my_crops/{farmId}"
    const val CROP_DETAILS = "crop_details/{cropId}"
    const val ADD_CROP_TASKS = "add_crop_tasks/{cropId}"
    const val CHANGE_PASSWORD = "change_password"
    const val PROFILE = "profile"
    const val FARMER_MANAGEMENT = "farmer_management/{farmerId}"
    const val FARMER_PROFILE = "farmer_profile/{farmerId}"
    const val FARM_SETUP = "farm_setup"
    const val FARM_SELECTION = "farm_selection/{farmerId}"
    const val FARM_FENCING = "farm_fencing/{farmId}"
    const val CROP_SELECTION = "crop_selection/{farmId}"
    const val WEATHER = "weather"
    const val ADVISORY_DASHBOARD = "advisory_dashboard"
    const val PEST_DETECTION = "pest_detection"
    const val SOIL_ANALYSIS = "soil_analysis"
    const val DISEASE_DETECTION = "disease_detection"
    const val TRAINING_CATALOG = "training_catalog"
    const val TRAINING_DETAILS = "training_details/{trainingId}"
    const val VIDEO_PLAYER = "video_player/{videoUrl}"
    const val INPUT_TRACKING = "input_tracking/{cropId}/{farmId}/{farmerId}"
    const val SEASONAL_PLANNING = "seasonal_planning/{farmId}"
    const val COMPLIANCE_TRACKING = "compliance_tracking/{farmId}"
    const val SOIL_PROFILE = "soil_profile/{farmId}"
    const val PLOT_MANAGEMENT = "farm_plots/{farmId}"
    const val REVENUE = "revenue/{farmerId}"


   // Helper functions
    fun dashboard(farmId: String): String {
        require(farmId.isNotBlank()) {
            "Attempted to navigate to dashboard with empty farmId"
        }
        return "dashboard/$farmId"
    }
    fun farmSelection(farmerId: String) = "farm_selection/$farmerId"
    fun farmerProfile(farmerId: String) = "farmer_profile/$farmerId"
    fun farmFencing(farmId: String?) = "farm_fencing/${farmId ?: "null"}"
    fun cropSelection(farmId: String) = "crop_selection/$farmId"
    fun cropDetails(cropId: String) = "crop_details/$cropId"
    fun trainingDetails(trainingId: String) = "training_details/$trainingId"
    fun videoPlayer(videoUrl: String) = "video_player/$videoUrl"
    fun farmerManagement(farmerId: String) = "farmer_management/$farmerId"
    fun addCropTasks(cropId: String) = "add_crop_tasks/$cropId"
    fun myCrops(farmId: String) = "my_crops/$farmId"
    fun inputTracking(cropId: String, farmId: String, farmerId: String) = "input_tracking/$cropId/$farmId/$farmerId"
    fun seasonalPlanning(farmId: String) = "seasonal_planning/$farmId"
    fun complianceTracking(farmId: String) = "compliance_tracking/$farmId"
    fun soilProfile(farmId: String) = "soil_profile/$farmId"
    fun plotManagement(farmId: String) = "farm_plots/$farmId"
    fun revenue(farmerId: String) = "revenue/$farmerId"

}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    navController: NavHostController,
    userSession: UserSessionManager
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController, userSession = userSession)
        }

        // Auth Flow
        authNavGraph(
            navController = navController,
            onNavigateToDashboard = {farmerId ->
                navController.navigate(Routes.farmSelection(farmerId)) {
                    popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = true }
                }
            }
        )


// --- NEW: FARM SELECTION ---
        // --- UPDATED: FARM SELECTION ---
        composable(
            route = Routes.FARM_SELECTION,
            arguments = listOf(navArgument("farmerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmerId = backStackEntry.arguments?.getString("farmerId") ?: ""
            val scope = rememberCoroutineScope()

            FarmSelectionScreen(
                userId = farmerId,
                navController = navController,
                onFarmSelected = {farmId ->
                    scope.launch {
                        userSession.setCurrentFarmId(farmId)
                    }
                    navController.navigate(Routes.dashboard(farmId)) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }


        // In AppNavigation.kt
        composable(
            route = Routes.DASHBOARD,
            arguments = listOf(
                navArgument("farmId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: ""

            DashboardScreen(
                navController = navController,
                farmId = farmId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


// Updated Farmer Profile to accept the ID argument
        composable(
            route = Routes.FARMER_PROFILE,
            arguments = listOf(navArgument("farmerId") { type = NavType.StringType })
        ) { backStackEntry ->

            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                }
            )
        }


         composable(Routes.CHANGE_PASSWORD) {
             ChangePasswordScreen(onNavigateBack = { navController.popBackStack() })
         }



        // Farm Management
        composable(Routes.FARM_SETUP) {
            // Prefer the farmerId from the previous screen (FarmSelection); fall back to session
            val farmerIdForReturn = remember {
                navController.previousBackStackEntry?.arguments?.getString("farmerId")
                    ?: userSession.getCurrentUserIdSync().orEmpty()
            }

            FarmSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetupComplete = { newFarmId ->
                    val target = Routes.farmSelection(farmerIdForReturn)
                    navController.navigate(target) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // --- UPDATED: FARM FENCING ---
        // Now it uses the specific farmId passed from the dashboard
        composable(
            route = Routes.FARM_FENCING,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId")
            FarmFencingScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() },
                onFarmSaved = {
                    navController.popBackStack()
                }
            )
        }


//cropSelection
        composable(
            route = Routes.CROP_SELECTION,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
        CropSelectionScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Inside your NavHost
        composable(
            route = Routes.MY_CROPS,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: ""
              MyCropsScreen(
                farmId = farmId,
                navController = navController,

                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable(
            route = Routes.CROP_DETAILS,
            arguments = listOf(navArgument("cropId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cropId = backStackEntry.arguments?.getString("cropId") ?: return@composable

            CropDetailsScreen(
                cropId = cropId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTask = {
                    navController.navigate(Routes.addCropTasks(cropId))
                },
                onNavigateToInputTracking = { cropId, farmId, farmerId ->
                    navController.navigate(Routes.inputTracking(cropId, farmId, farmerId))
                }
            )
        }

        composable(
            route = Routes.ADD_CROP_TASKS,
            arguments = listOf(navArgument("cropId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cropId = backStackEntry.arguments?.getString("cropId") ?: return@composable

            AddCropTasksScreen(
                cropId = cropId,
                onNavigateBack = { navController.popBackStack() },
                onActivitySaved = {
                    navController.popBackStack() // clean & simple
                }
            )
        }

        composable(
            route = Routes.INPUT_TRACKING,
            arguments = listOf(
                navArgument("cropId") { type = NavType.StringType },
                navArgument("farmId") { type = NavType.StringType },
                navArgument("farmerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cropId = backStackEntry.arguments?.getString("cropId") ?: return@composable
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            val farmerId = backStackEntry.arguments?.getString("farmerId") ?: return@composable
            InputTrackingScreen(
                cropId = cropId,
                farmId = farmId,
                farmerId = farmerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SEASONAL_PLANNING,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            SeasonalPlanningScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.COMPLIANCE_TRACKING,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            ComplianceTrackingScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SOIL_PROFILE,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            SoilProfileScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PLOT_MANAGEMENT,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            PlotManagementScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() },
                onPlotClick = { /* future: plot detail with rotation */ }
            )
        }

        composable(
            route = Routes.REVENUE,
            arguments = listOf(navArgument("farmerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmerId = backStackEntry.arguments?.getString("farmerId") ?: return@composable
            RevenueScreen(
                farmerId = farmerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Advisory Services
        composable(Routes.ADVISORY_DASHBOARD) {
            AdvisoryDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPestDetection = {
                    navController.navigate(Routes.PEST_DETECTION)
                },
                onNavigateToSoilAnalysis = {
                    navController.navigate(Routes.SOIL_ANALYSIS)
                },
                onNavigateToDiseaseDetection = {
                    navController.navigate(Routes.DISEASE_DETECTION)
                }
            )
        }
        composable(Routes.PEST_DETECTION) { PestDetectionScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.SOIL_ANALYSIS) { SoilAnalysisScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.DISEASE_DETECTION) { DiseaseDetectionScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.WEATHER) { WeatherScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.TRAINING_CATALOG) {
            TrainingScreen(
                onNavigateToVideoPlayer = { videoUrl ->
                    navController.navigate(Routes.videoPlayer(videoUrl))
                },
                onNavigateToVideoDetail = { trainingId ->
                    navController.navigate(Routes.trainingDetails(trainingId))
                }
             )
        }

        composable(
            route = Routes.TRAINING_DETAILS,
            arguments = listOf(navArgument("trainingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trainingId = backStackEntry.arguments?.getString("trainingId") ?: return@composable

            TrainingDetailsScreen(
                trainingId = trainingId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVideoPlayer = { videoUrl ->
                    navController.navigate(Routes.videoPlayer(videoUrl))
                }
            )
        }

        composable(
            route = Routes.VIDEO_PLAYER,
            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: return@composable
            VideoPlayerScreen(videoUrl = videoUrl, onNavigateBack = { navController.popBackStack() })
        }

        // VERIFIEDFARMER Routes
        composable(
            route = Routes.FARMER_MANAGEMENT,
            arguments = listOf(navArgument("farmerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmerId = backStackEntry.arguments?.getString("farmerId") ?: return@composable

            FarmerManagementScreen(
                farmerId = farmerId,
                onNavigateToProfile = {
                    navController.navigate(Routes.farmerProfile(farmerId))
                },
                onNavigateToFarmFencing = { farmId ->
                    navController.navigate(Routes.farmFencing(farmId))
                }
            )
        }

        // Certification, Quality, Product Auth — only for VERIFIEDFARMER
        certificationNavGraph(navController)
         qualityNavGraph(navController)
         productAuthNavGraph(navController)
    }
}



@Composable
fun UnauthorizedScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Access Denied", style = MaterialTheme.typography.headlineMedium)
            Text("You don't have permission to view this page")
            Button(onClick = { /* Go home */ }) { Text("Go to Dashboard") }
        }
    }
}
