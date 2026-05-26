package com.goldleaf.certification.domain.repository


import com.goldleaf.core.data.local.LabTest
import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.ProductBatchEntity
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    // Create batch
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

    // Get batches
    fun getAllBatches(): Flow<List<ProductBatchEntity>>
    fun getBatchById(batchId: String): Flow<ProductBatchEntity?>
    fun getBatchByNumber(batchNumber: String): Flow<ProductBatchEntity?>

    // Server sync
    suspend fun syncBatches(farmerId: String): Result<List<ProductBatchEntity>>

    // Read-only from server
    suspend fun getLabTests(batchId: String): Result<List<LabTest>>
    suspend fun getBlockchainRecord(batchId: String): Result<BlockchainRecord?>

    // Consumer verification
    suspend fun verifyProduct(batchNumber: String): Result<VerificationResult>
}

data class VerificationResult(
    val isValid: Boolean,
    val message: String,
    val batch: ProductBatchEntity?,
    val blockchainRecord: BlockchainRecord?,
    val labTests: List<LabTest>
)