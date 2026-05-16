package com.goldleaf.core.data.local



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journey_events")
data class JourneyEventEntity(
    @PrimaryKey val id: String,
    val journeyId: String,
    val eventType: String, // DISPATCH, CHECKPOINT, ARRIVAL, DELAY
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val description: String?,
    val verifiedBy: String?,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)
