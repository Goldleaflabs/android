package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "audit_records",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"])]  // ADD THIS
)

data class AuditRecordEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val auditType: String,
    val auditorName: String,
    val auditDate: Long,
    val findings: String, // JSON string
    val score: Double?,
    val status: String, // PASSED, FAILED, PENDING
    val reportUrl: String?,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null
)
