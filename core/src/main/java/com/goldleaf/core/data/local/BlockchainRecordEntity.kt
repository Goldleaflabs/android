package com.goldleaf.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "blockchain_records",
    indices = [Index(value = ["entityId"])]  // ADD THIS
)

data class BlockchainRecordEntity(
    @PrimaryKey val id: String,
    val transactionHash: String,
    val batchId: String,
    val blockNumber: Long?,
    val recordType: BlockchainRecordType, // HARVEST, QUALITY_CHECK, SHIPMENT, etc.
    val entityId: String, // Related batch/crop/farm ID
    val data: String, // JSON string of blockchain data
    val timestamp: Long,
    val status: BlockchainStatus, // PENDING, CONFIRMED, FAILED
    val lastSyncTime: Long = System.currentTimeMillis(),
    val farmerId: String? = null,
    val farmId: String? = null
)

enum class BlockchainStatus {
    PENDING, CONFIRMED, FAILED
}

enum class BlockchainRecordType {
    HARVEST, QUALITY_CHECK, SHIPMENT
}
