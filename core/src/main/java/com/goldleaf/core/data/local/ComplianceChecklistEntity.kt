package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "compliance_checklist")
data class ComplianceChecklistEntity(
    @PrimaryKey @SerializedName("id") val id: String,
    @SerializedName("farm_id") val farmId: String,
    @SerializedName("farmer_id") val farmerId: String?,
    @SerializedName("category") val category: String,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String,
    @SerializedName("evidence_url") val evidenceUrl: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("reviewed_by") val reviewedBy: String?,
    @SerializedName("reviewed_at") val reviewedAt: Long?,
    @SerializedName("due_date") val dueDate: Long?,
    @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("last_sync_time") val lastSyncTime: Long = System.currentTimeMillis()
)
