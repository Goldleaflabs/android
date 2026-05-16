package com.goldleaf.feature.farmermanagement.ui.dashboard

import com.goldleaf.core.auth.UserRole

data class DashboardFarmer(
    val id: String,
    val name: String,
    val userRole: UserRole,
    val farmname: String?
)