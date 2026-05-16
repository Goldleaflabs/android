package com.goldleaf.certification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.goldleaf.certification.ui.verification.ConsumerVerificationScreen

fun NavGraphBuilder.productAuthNavGraph(navController: NavHostController) {
    navigation(
        startDestination = CertificationRoutes.ConsumerVerificationNoBatch.route,
        route = CertificationRoutes.ProductAuthGraph.route
    ) {

        // No batch ID version
        composable(
            route = CertificationRoutes.ConsumerVerificationNoBatch.route
        ) {
            ConsumerVerificationScreen(
                batchId = null,
                navController = navController
            )
        }

        // Batch ID version
        composable(
            route = CertificationRoutes.ConsumerVerification.route,
            arguments = listOf(
                navArgument("batchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
            ConsumerVerificationScreen(
                batchId = batchId,
                navController = navController
            )
        }
    }
}
