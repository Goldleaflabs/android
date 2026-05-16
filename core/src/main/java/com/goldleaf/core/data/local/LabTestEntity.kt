package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab_tests")
data class LabTestEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val testType: String,
    val labName: String,
    val testDate: Long,
    val results: String, // JSON string
    val status: Teststatus, // PENDING, PASSED, FAILED
    val reportUrl: String?,
    val certifiedBy: String?,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)


enum class Teststatus {
    PASSED, FAILED, WARNING
}
