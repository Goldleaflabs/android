package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certification_requirements")
data class CertificationRequirementEntity(
    @PrimaryKey val id: String,
    val certificationId: String,
    val requirementName: String,
    val description: String,
    val isMandatory: Boolean,
    val status: CertificationStatus, // PENDING, MET, NOT_MET
    val verifiedAt: Long?,
    val verifiedBy: String?,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)

