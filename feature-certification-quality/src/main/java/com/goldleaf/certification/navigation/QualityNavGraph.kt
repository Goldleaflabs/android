package com.goldleaf.certification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.goldleaf.certification.ui.quality.QualityDashboardScreen

fun NavGraphBuilder.qualityNavGraph(navController: NavHostController) {
    navigation(
        startDestination = CertificationRoutes.QualityDashboard.route,
        route = CertificationRoutes.QualityGraph.route
    ) {
        composable(CertificationRoutes.QualityDashboard.route) {
            QualityDashboardScreen(navController = navController)
        }
    }
}