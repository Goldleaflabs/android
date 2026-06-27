package com.goldleaf.core.data.mapper

import com.goldleaf.core.data.local.ProductBatchEntity
import com.goldleaf.core.data.dto.BatchDto
import com.goldleaf.core.data.local.BlockchainStatus

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
        blockchainStatus = blockchainStatus.name,
        blockchainTimestamp = blockchainTimestamp,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        lastSyncTime = System.currentTimeMillis()
    )
}

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
        blockchainStatus = try { BlockchainStatus.valueOf(blockchainStatus) } catch (_: Exception) { BlockchainStatus.PENDING },
        blockchainTimestamp = blockchainTimestamp,
        blockchainHash = blockchainHash,
        updatedAt = System.currentTimeMillis().toString()
    )
}
