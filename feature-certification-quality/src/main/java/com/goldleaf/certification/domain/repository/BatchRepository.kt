package com.goldleaf.certification.domain.repository

import com.goldleaf.core.data.local.*
import com.goldleaf.core.domain.model.VerificationResult
import com.goldleaf.feature.cropmanagement.ui.activity.CropInfo
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    suspend fun createBatch(
        batchNumber: String,
        productType: String,
        quantity: Double,
        unit: String,
        harvestDate: String,
        farmerId: String,
        farmerName: String,
        cropId: String = ""
    ): Result<ProductBatchEntity>
    suspend fun syncBatches(farmerId: String): Result<List<ProductBatchEntity>>
    suspend fun loadBatchesForFarmer(farmerId: String): List<ProductBatchEntity>
    suspend fun loadReadyCropsForFarmer(farmerId: String): List<CropInfo>
    suspend fun getLabTests(batchId: String): Result<List<LabTest>>
    suspend fun getBlockchainRecord(batchId: String): Result<BlockchainRecord?>
    suspend fun verifyProduct(batchNumber: String): Result<VerificationResult>
    fun getAllBatches(): Flow<List<ProductBatchEntity>>
    fun getBatchById(batchId: String): Flow<ProductBatchEntity?>
    fun getBatchByNumber(batchNumber: String): Flow<ProductBatchEntity?>
}

data class CreateBatchRequest(
    val cropId: String,
    val farmId: String,
    val quantity: Double,
    val unit: String,
    val harvestDate: Long,
    val farmerId: String
)
