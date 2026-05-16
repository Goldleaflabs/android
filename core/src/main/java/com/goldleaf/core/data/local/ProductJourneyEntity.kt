package com.goldleaf.core.data.local



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_journeys")
data class ProductJourneyEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val startLocation: String,
    val currentLocation: String,
    val destinationLocation: String,
    val startDate: Long,
    val estimatedArrival: Long?,
    val actualArrival: Long?,
    val status: JourneyStatus, // IN_TRANSIT, DELIVERED, DELAYED
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)

enum class JourneyStatus {
    IN_TRANSIT, DELIVERED, DELAYED
}
