package com.goldleaf.core.data.dto.auth

import android.os.Parcelable
import com.goldleaf.core.auth.UserRole
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardFarmer(
    val id: String,
    val name: String,
    val userRole: UserRole
) : Parcelable