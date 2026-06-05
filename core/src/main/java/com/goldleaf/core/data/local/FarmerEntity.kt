package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.goldleaf.core.data.dto.farm.FarmerPreferences

@Entity(tableName = "farmers")
@TypeConverters(FarmerPreferencesConverter::class)
data class FarmerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val location: String? = null,
    val district: String? = null,
    val region: String? = null,
    val street: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastSyncTime: Long = 0L,
    val preferences: FarmerPreferences = FarmerPreferences(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null,
    val userRole: String? = null
)
