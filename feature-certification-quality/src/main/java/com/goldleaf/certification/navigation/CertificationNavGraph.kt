package com.goldleaf.certification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.goldleaf.certification.ui.dashboard.CertificationDashboardScreen
import com.goldleaf.certification.ui.details.CertificationDetailsScreen
import com.goldleaf.certification.ui.details.BatchDetailsScreen

fun NavGraphBuilder.certificationNavGraph(navController: NavHostController) {
    navigation(

        startDestination = CertificationRoutes.CertificationDashboard.route,
        route = CertificationRoutes.CertificationGraph.route
    ) {
        composable(CertificationRoutes.CertificationDashboard.route) {
            CertificationDashboardScreen(navController = navController)
        }

        /** CERTIFICATION DETAILS SCREEN **/
        composable(
            route = CertificationRoutes.CertificationDetails.route,
            arguments = listOf(
                navArgument("certId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val certId = backStackEntry.arguments?.getString("certId") ?: ""
            CertificationDetailsScreen(
                certId = certId,
                navController = navController
            )
        }

        /** BATCH DETAILS SCREEN **/
        composable(
            route = CertificationRoutes.BatchDetails.route,
            arguments = listOf(
                navArgument("batchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
            BatchDetailsScreen(
                batchId = batchId,
                navController = navController
            )
        }
    }
}