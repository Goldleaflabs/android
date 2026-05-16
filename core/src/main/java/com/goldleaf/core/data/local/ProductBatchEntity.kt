package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_batches")
data class ProductBatchEntity(
    @PrimaryKey val id: String,
    val batchNumber: String,
    val cropId: String = "",
    val farmId: String = "",
    val quantity: Double = 0.0,
    val farmerId: String = "",
    val farmerName: String = "",
    val unit: String = "",
    val harvestDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val blockchainRecordId: String? = null,
    val status: ProductStatus = ProductStatus.HARVESTED,
    val qrCode: String? = null,
    val productType: String = "",
    val qualityGrade: String? = null,
    val blockchainStatus: BlockchainStatus = BlockchainStatus.PENDING,
    val blockchainTimestamp: String? = null,
    val blockchainHash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTime: Long = System.currentTimeMillis(),
    val syncedToServer: Boolean = false
)




enum class ProductStatus {
    HARVESTED, PROCESSING, PACKAGED, SHIPPED, DELIVERED
}