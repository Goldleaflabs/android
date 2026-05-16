package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crop_activities",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["id"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cropId"])]
)
data class CropActivity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val activityType: String,
    val date: String,
    val description: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val cost: Double? = null,
    val notes: String? = null,
    val createdAt: Long,
    val farmerId: String? = null,
    val farmId: String? = null
)
