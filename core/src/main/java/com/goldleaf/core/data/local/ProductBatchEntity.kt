package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "product_batches")
data class ProductBatchEntity(
    @PrimaryKey val id: String,
    @SerializedName("batchCode") val batchNumber: String,
    val cropId: String = "",
    val farmId: String = "",
    val quantity: Double = 0.0,
    val farmerId: String = "",
    val farmerName: String = "",
    val unit: String = "",
    val harvestDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val blockchainRecordId: String? = null,
    val status: String = "HARVESTED",
    val qrCode: String? = null,
    val productType: String = "",
    @SerializedName("quality") val qualityGrade: String? = null,
    val blockchainStatus: String = "PENDING",
    val blockchainTimestamp: String? = null,
    val blockchainHash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncTime: Long = System.currentTimeMillis()
)
