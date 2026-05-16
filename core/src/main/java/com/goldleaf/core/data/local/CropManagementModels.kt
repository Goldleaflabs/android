// CropManagementModels.kt
package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class CropVariety(
    val id: String,
    val name: String,
    val cropType: String,
    val description: String,
    val growthDuration: Int, // days
    val optimalTemperature: TemperatureRange,
    val waterRequirements: String,
    val soilType: String,
    val expectedYieldPerHectare: Double
)

data class TemperatureRange(
    val min: Double,
    val max: Double,
    val unit: String = "°C"
)

data class CropGrowthStage(
    val id: String,
    val cropId: String,
    val stage: GrowthStage,
    val startDate: String,
    val endDate: String?,
    val description: String,
    val tasks: List<CropTask>,
    val isCompleted: Boolean
)

data class CropTask(
    val id: String,
    val cropId: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: TaskPriority,
    val category: TaskCategory,
    val isCompleted: Boolean,
    val completedDate: String?,
    val notes: String?
)

data class CropMonitoringRecord(
    val id: String,
    val cropId: String,
    val recordDate: String,
    val healthStatus: HealthStatus,
    val moistureLevel: Double?,
    val pestObservations: String?,
    val diseaseObservations: String?,
    val weatherConditions: String?,
    val photos: List<String>,
    val notes: String?,
    val recordedBy: String?
)

data class YieldAnalytics(
    val totalArea: Double? = 0.0,
    val totalExpectedYield: Double? = 0.0,
    val totalActualYield: Double? = 0.0,
    val averageYieldPerHectare: Double? = 0.0,
    val completedCrops: Int,
    val activeCrops: Int,
    val yieldEfficiency: Double? = 0.0, // actual vs expected percentage
    val topPerformingCrops: List<CropPerformance>
)


data class CropPerformance(
    val cropId: String,
    val cropName: String,
    val variety: String,
    val actualYield: Double,
    val expectedYield: Double,
    val efficiency: Double,
    val area: Double
)

data class CropCalendar(
    val cropId: String,
    val events: List<CalendarEvent>
)

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: String,
    val eventType: EventType,
    val description: String?,
    val isCompleted: Boolean
)

@Entity(tableName = "crop_varieties")
data class CropVarietyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val cropType: String,
    val description: String,
    val growthDuration: Int,
    val minTemperature: Double,
    val maxTemperature: Double,
    val waterRequirements: String,
    val soilType: String,
    val expectedYieldPerHectare: Double,
    val farmerId: String? = null,
    val farmId: String? = null
)

@Entity(tableName = "crop_tasks")
data class CropTaskEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: TaskPriority,
    val category: TaskCategory,
    val isCompleted: Boolean,
    val completedDate: String?,
    val notes: String?,
    val farmerId: String? = null,
    val farmId: String? = null
)

@Entity(tableName = "crop_monitoring_records")
data class CropMonitoringRecordEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val recordDate: String,
    val healthStatus: HealthStatus,
    val moistureLevel: Double?,
    val pestObservations: String?,
    val diseaseObservations: String?,
    val weatherConditions: String?,
    val photos: String, // JSON string of photo URLs
    val notes: String?,
    val recordedBy: String?,
    val farmerId: String? = null,
    val farmId: String? = null
)

data class CropDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("variety")
    val variety: String,
    @SerializedName("planting_date")
    val plantingDate: String,
    @SerializedName("harvest_date")
    val harvestDate: String?,
    @SerializedName("location")
    val location: String,
    @SerializedName("area")
    val area: Double,
    @SerializedName("status")
    val status: CropStatus,
    @SerializedName("expected_yield")
    val expectedYield: Double?,
    @SerializedName("actual_yield")
    val actualYield: Double?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("farmId")
    val farmId: String

)

data class CropVarietyDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("crop_type")
    val cropType: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("growth_duration")
    val growthDuration: Int,
    @SerializedName("optimal_temperature")
    val optimalTemperature: TemperatureRangeDto,
    @SerializedName("water_requirements")
    val waterRequirements: String,
    @SerializedName("soil_type")
    val soilType: String,
    @SerializedName("expected_yield_per_hectare")
    val expectedYieldPerHectare: Double
)

data class TemperatureRangeDto(
    @SerializedName("min")
    val min: Double,
    @SerializedName("max")
    val max: Double,
    @SerializedName("unit")
    val unit: String
)

data class CropTaskDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("crop_id")
    val cropId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("due_date")
    val dueDate: String,
    @SerializedName("priority")
    val priority: TaskPriority,
    @SerializedName("category")
    val category: TaskCategory,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("completed_date")
    val completedDate: String?,
    @SerializedName("notes")
    val notes: String?
)

data class YieldAnalyticsDto(
    @SerializedName("total_area")
    val totalArea: Double,
    @SerializedName("total_expected_yield")
    val totalExpectedYield: Double,
    @SerializedName("total_actual_yield")
    val totalActualYield: Double,
    @SerializedName("average_yield_per_hectare")
    val averageYieldPerHectare: Double,
    @SerializedName("completed_crops")
    val completedCrops: Int,
    @SerializedName("active_crops")
    val activeCrops: Int,
    @SerializedName("yield_efficiency")
    val yieldEfficiency: Double,
    @SerializedName("top_performing_crops")
    val topPerformingCrops: List<CropPerformanceDto>
)

data class CropPerformanceDto(
    @SerializedName("crop_id")
    val cropId: String,
    @SerializedName("crop_name")
    val cropName: String,
    @SerializedName("variety")
    val variety: String,
    @SerializedName("actual_yield")
    val actualYield: Double,
    @SerializedName("expected_yield")
    val expectedYield: Double,
    @SerializedName("efficiency")
    val efficiency: Double,
    @SerializedName("area")
    val area: Double
)

data class CreateCropRequest(
    val name: String,
    val variety: String,
    val plantingDate: String,
    val location: String,
    val area: Double,
    val expectedYield: Double?,
    val notes: String?
)

data class UpdateCropRequest(
    val name: String?,
    val variety: String?,
    val harvestDate: String?,
    val location: String?,
    val area: Double?,
    val status: CropStatus?,
    val expectedYield: Double?,
    val actualYield: Double?,
    val notes: String?,
    val imageUrl: String?
)

data class CropResponse(
    val success: Boolean,
    val message: String,
    val data: CropDto?
)

data class CropsListResponse(
    val success: Boolean,
    val message: String,
    val data: List<CropDto>
)

data class YieldAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: YieldAnalyticsDto?
)
