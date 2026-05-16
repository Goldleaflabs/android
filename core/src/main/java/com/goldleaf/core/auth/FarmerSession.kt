package com.goldleaf.core.auth

data class FarmerSession(
    val farmerId: String,
    val farmerName: String,
    val email: String? = null,
    val phoneNumber: String? = null
)