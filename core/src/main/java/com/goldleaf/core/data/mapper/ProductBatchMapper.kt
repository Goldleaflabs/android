package com.goldleaf.core.data.mapper

import com.goldleaf.core.data.local.ProductBatchEntity
import com.goldleaf.core.data.dto.BatchDto

// ✅ From remote DTO → Domain model
fun BatchDto.toDomain(): ProductBatchEntity {
    return ProductBatchEntity(
        id = id,
        batchNumber = batchNumber,
        productType = productType,
        quantity = quantity,
        unit = unit,
        harvestDate = harvestDate,
        farmerId = farmerId,
        farmerName = farmerName,
        qualityGrade = qualityGrade,
        blockchainHash = blockchainHash,
        blockchainStatus = blockchainStatus,
        blockchainTimestamp = blockchainTimestamp,
        createdAt = System.currentTimeMillis(),
        lastSyncTime = System.currentTimeMillis(),
        syncedToServer = true
    )
}

// ✅ From local entity → Domain model
fun ProductBatchEntity.toDomain(): BatchDto {
    return BatchDto(
        id = id,
        batchNumber = batchNumber,
        quantity = quantity,
        farmerId = farmerId,
        farmerName = farmerName,
        unit = unit,
        harvestDate = harvestDate,
        productType = productType,
        qualityGrade = qualityGrade,
        blockchainStatus = blockchainStatus,
        blockchainTimestamp = blockchainTimestamp,
        blockchainHash = blockchainHash,
        updatedAt = System.currentTimeMillis().toString()
    )
}
