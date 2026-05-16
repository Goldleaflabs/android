package com.goldleaf.core.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "certifications")
data class CertificationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String?,
    val issuingAuthority: String,
    val validFrom:  LocalDateTime,
    val validUntil: LocalDateTime?,
    val status: String? ,
    val farmerId: String?,
    val certificateUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmId: String? = null
)
