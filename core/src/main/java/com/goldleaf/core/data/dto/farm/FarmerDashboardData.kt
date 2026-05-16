package com.goldleaf.core.data.dto.farm

import java.time.LocalDateTime

data class FarmerDashboardData(
    // Farmer reference
    val farmer: Farmer,
    val totalFarms: Int = 0,
    val totalLandSize: Double = 0.0,
    val landSizeUnit: LandUnit = LandUnit.ACRES,
    val activeFarms: List<FarmSummary> = emptyList(),

    // Crop statistics
    val totalCrops: Int = 0,
    val activeCrops: Int = 0,
    val mainCrops: List<String> = emptyList(),

    // Activities & Tasks
    val recentActivities: List<DashboardActivity> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),

    // Weather (optional - if weather module available)
    val weatherSummary: WeatherSummary? = null,

    // Notifications
    val notifications: List<Notification> = emptyList(),

    // Timestamps
    val memberSince: LocalDateTime,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)


/**
 * Dashboard activity item - only includes actual app features
 */
data class DashboardActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String? = null,
    val timestamp: LocalDateTime,
    val farmName: String? = null
)

/**
 * Activity types based on current app features only
 */
enum class ActivityType {
    FARM_REGISTERED,      // When farmer registers
    FARM_ADDED,           // When a new farm is added
    FARM_UPDATED,         // When farm details are updated
    BOUNDARY_MAPPED,      // When farm boundary is completed
    CROP_ADDED,           // When crops are added to farm
    CROP_UPDATED,         // When crop information is updated
    WATER_SOURCE_ADDED,   // When water source is added
    FACILITY_ADDED,       // When facility is added
    PROFILE_UPDATED       // When profile is updated
}



/**
 * Farming type enum (from FarmFencingViewModel)
 */
enum class FarmingType {
    CROP_FARMING,
    LIVESTOCK,
    MIXED,
    HORTICULTURE,
    DAIRY,
    POULTRY,
    FISH_FARMING,
    ORGANIC
}

/**
 * Farm status enum
 */
enum class FarmStatus {
    ACTIVE,
    INACTIVE,
    PENDING_VERIFICATION
}