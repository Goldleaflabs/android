package com.goldleaf.farmerportal.navigation


import com.goldleaf.core.auth.UserRole

data class RouteAccess(
    val route: String,
    val allowedRoles: List<UserRole>,
    val label: String,
    val isBottomNavItem: Boolean = false
)

object AppRoutes {
    // Farmer Routes
    val DASHBOARD = RouteAccess(
        route = Routes.DASHBOARD,
        allowedRoles = listOf(UserRole.FARMER, UserRole.VERIFIEDFARMER),
        label = "Dashboard",
        isBottomNavItem = true
    )



    val CROP_MANAGEMENT = RouteAccess(
        route = Routes.CROP_SELECTION,
        allowedRoles = listOf(UserRole.FARMER, UserRole.VERIFIEDFARMER),
        label = "My Crops",
        isBottomNavItem = true
    )

    val WEATHER = RouteAccess(
        route = Routes.WEATHER,
        allowedRoles = listOf(UserRole.FARMER),
        label = "Weather",
        isBottomNavItem = true
    )

    val ADVISORY = RouteAccess(
        route = Routes.ADVISORY_DASHBOARD,
        allowedRoles = listOf(UserRole.FARMER),
        label = "Advisory",
        isBottomNavItem = true
    )

    val TRAINING = RouteAccess(
        route = Routes.TRAINING_CATALOG,
        allowedRoles = listOf(UserRole.FARMER),
        label = "Training",
        isBottomNavItem = false
    )

    val PROFILE = RouteAccess(
        route = Routes.FARMER_PROFILE,
        allowedRoles = listOf(UserRole.FARMER, UserRole.VERIFIEDFARMER),
        label = "Profile",
        isBottomNavItem = false
    )

    // VERIFIEDFARMER-Only Routes
    val CERTIFICATION = RouteAccess(
        route = "certification_graph",
        allowedRoles = listOf(UserRole.VERIFIEDFARMER),
        label = "Certification",
        isBottomNavItem = true
    )

    val QUALITY = RouteAccess(
        route = "quality_graph",
        allowedRoles = listOf(UserRole.VERIFIEDFARMER),
        label = "Quality",
        isBottomNavItem = false
    )

    val VERIFICATION = RouteAccess(
        route = "product_auth_graph",
        allowedRoles = listOf(UserRole.VERIFIEDFARMER),
        label = "Verify Products",
        isBottomNavItem = false
    )

    // All available routes
    val allRoutes = listOf(
        DASHBOARD, CROP_MANAGEMENT, WEATHER, ADVISORY,
        TRAINING, PROFILE, CERTIFICATION, QUALITY, VERIFICATION
    )

    fun getRoutesForRole(role: UserRole): List<RouteAccess> {
        return allRoutes.filter { it.allowedRoles.contains(role) }
    }

    fun getBottomNavItemsForRole(role: UserRole): List<RouteAccess> {
        return allRoutes.filter {
            it.isBottomNavItem && it.allowedRoles.contains(role)
        }
    }
}
