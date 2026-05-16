package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index  // ✅ ADD THIS LINE

@Entity(
    tableName = "advisories",
    foreignKeys = [
        ForeignKey(
            entity = FarmerEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmerId"])]  // ADD THIS
)
data class AdvisoryEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val title: String,
    val content: String,
    val category: String, // WEATHER, PEST, DISEASE, MARKET, GENERAL
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val farmId: String? = null
)
