package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "advisories",
    indices = [Index(value = ["farmerId"])]
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
