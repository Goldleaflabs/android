package com.goldleaf.feature.farmermanagement.navigation


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.goldleaf.feature.farmermanagement.ui.auth.login.LoginScreen
import com.goldleaf.feature.farmermanagement.ui.auth.recovery.ForgotPasswordScreen
import com.goldleaf.feature.farmermanagement.ui.screens.RegistrationScreen

object AuthRoutes {
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onNavigateToDashboard: (String) -> Unit
) {
    navigation(
        startDestination = AuthRoutes.LOGIN,
        route = AuthRoutes.AUTH_GRAPH
    ) {
        // Login Screen
        composable(route = AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AuthRoutes.FORGOT_PASSWORD)
                },
                onNavigateToDashboard = { farmerId ->
                    onNavigateToDashboard(farmerId)
                }
            )
        }

        // NEW (RegistrationScreen with OTP steps)
        composable(AuthRoutes.REGISTER) {
            RegistrationScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationComplete = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Forgot Password Screen
        composable(route = AuthRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToLogin = {
                    navController.popBackStack(AuthRoutes.LOGIN, inclusive = false)
                }
            )
        }
    }
}

// Extension functions
fun NavController.navigateToLogin() {
    navigate(AuthRoutes.LOGIN) {
        popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = true }
    }
}

fun NavController.navigateToRegister() {
    navigate(AuthRoutes.REGISTER)
}

fun NavController.navigateToForgotPassword() {
    navigate(AuthRoutes.FORGOT_PASSWORD)
}
