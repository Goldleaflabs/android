package com.goldleaf.core.data.local

// FILE: feature-certification-quality/src/main/java/com/goldleaf/certification/domain/model/BlockchainRecord.kt

data class BlockchainRecord(
    val id: String,
    val batchId: String,
    val transactionHash: String,
    val blockNumber: Long,
    val network: String,
    val timestamp: String,
    val status: BlockchainStatus
)