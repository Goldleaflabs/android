package com.goldleaf.farmerportal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.goldleaf.core.auth.UserRole
import com.goldleaf.farmerportal.navigation.AppRoutes

private fun getIconForRoute(route: String): ImageVector {
    return when {
        route.contains("dashboard") -> Icons.Default.Home
        route.contains("crop") -> Icons.Default.Spa  // ✅ Changed from Grass
        route.contains("weather") -> Icons.Default.Cloud
        route.contains("advisory") -> Icons.Default.Lightbulb
        route.contains("profile") -> Icons.Default.Person
        route.contains("certification") -> Icons.Default.Verified  // ✅ Changed from VerifiedUser
        route.contains("quality") -> Icons.Default.CheckCircle
        route.contains("verification") -> Icons.Default.QrCode  // ✅ Changed from QrCode2
        else -> Icons.Default.Circle
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Get items for all farmer roles
    val items = AppRoutes.getBottomNavItemsForRole(UserRole.FARMER)

    if (items.isEmpty()) return

    NavigationBar {
        items.forEach { routeAccess ->
            NavigationBarItem(
                icon = {
                    Icon(
                        getIconForRoute(routeAccess.route),
                        contentDescription = routeAccess.label
                    )
                },
                label = { Text(routeAccess.label) },
                selected = currentRoute?.startsWith(routeAccess.route) == true,
                onClick = {
                    navController.navigate(routeAccess.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}