package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "seasonal_plans")
data class SeasonalPlanEntity(
    @PrimaryKey @SerializedName("id") val id: String,
    @SerializedName("farm_id") val farmId: String,
    @SerializedName("farmer_id") val farmerId: String?,
    @SerializedName("crop_id") val cropId: String?,
    @SerializedName("plot_id") val plotId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("event_type") val eventType: String,
    @SerializedName("start_date") val startDate: Long,
    @SerializedName("end_date") val endDate: Long?,
    @SerializedName("season") val season: String,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("completed_at") val completedAt: Long? = null,
    @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("last_sync_time") val lastSyncTime: Long = System.currentTimeMillis()
)
