package com.goldleaf.certification.data.repository

import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.local.dao.ProductBatchDao
import com.goldleaf.core.data.local.ProductBatchEntity
import com.goldleaf.certification.data.remote.CertificationApiService
import com.goldleaf.certification.domain.repository.BatchRepository
import com.goldleaf.core.domain.model.VerificationResult
import com.goldleaf.core.data.dto.CreateBatchRequest
import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.core.data.local.BlockchainStatus
import com.goldleaf.core.data.mapper.*
import com.goldleaf.core.data.local.LabTest
import com.goldleaf.core.data.dto.toDomainModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class BatchRepositoryImpl @Inject constructor(
    private val apiService: CertificationApiService,
    private val batchDao: ProductBatchDao,
    private val userSession: UserSessionManager
) : BatchRepository {

    private val authToken: String
        get() {
            val token = userSession.getAuthTokenSync()
            return if (token != null) "Bearer $token" else ""
        }

    override suspend fun createBatch(
        batchNumber: String,
        productType: String,
        quantity: Double,
        unit: String,
        harvestDate: String,
        farmerId: String,
        farmerName: String,
        cropId: String
    ): Result<ProductBatchEntity> {
        return try {
            val harvestDateLong = LocalDate.parse(harvestDate)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val entity = ProductBatchEntity(
                id = UUID.randomUUID().toString(),
                batchNumber = batchNumber,
                productType = productType,
                quantity = quantity,
                unit = unit,
                harvestDate = harvestDateLong,
                farmerId = farmerId,
                farmerName = farmerName,
                cropId = cropId
            )

            batchDao.insertBatch(entity)

            try {
                val request = CreateBatchRequest(
                    batchNumber, productType, quantity, unit,
                    harvestDateLong, farmerId, farmerName
                )
                val response = apiService.createBatch(authToken, request)
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    val updatedEntity = entity.copy(
                        id = dto.id,
                        blockchainHash = dto.blockchainHash,
                        blockchainStatus = dto.blockchainStatus,
                        syncedToServer = true
                    )
                    batchDao.updateBatch(updatedEntity)
                    Result.success(updatedEntity)
                } else {
                    Result.success(entity)
                }
            } catch (e: Exception) {
                Result.success(entity)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllBatches(): Flow<List<ProductBatchEntity>> {
        return batchDao.getAllBatches().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBatchById(batchId: String): Flow<ProductBatchEntity?> {
        return batchDao.getBatchById(batchId).map { it?.toDomain() }
    }

    override fun getBatchByNumber(batchNumber: String): Flow<ProductBatchEntity?> {
        return batchDao.getBatchByNumber(batchNumber).map { it?.toDomain() }
    }

    override suspend fun loadBatchesForFarmer(farmerId: String): List<ProductBatchEntity> {
        return batchDao.getAllBatches().map { list -> list.map { it.toDomain() } }.first()
    }

    override suspend fun loadReadyCropsForFarmer(farmerId: String): List<com.goldleaf.feature.cropmanagement.ui.activity.CropInfo> {
        return emptyList()
    }

    override suspend fun syncBatches(farmerId: String): Result<List<ProductBatchEntity>> {
        return try {
            val response = apiService.getFarmerBatches(authToken, farmerId)
            if (response.isSuccessful && response.body() != null) {
                val batches = response.body()!!.map { dto ->
                    ProductBatchEntity(
                        id = dto.id,
                        batchNumber = dto.batchNumber,
                        productType = dto.productType,
                        quantity = dto.quantity,
                        unit = dto.unit,
                        harvestDate = dto.harvestDate,
                        farmerId = dto.farmerId,
                        farmerName = dto.farmerName,
                        qualityGrade = dto.qualityGrade,
                        blockchainHash = dto.blockchainHash,
                        blockchainStatus = dto.blockchainStatus,
                        blockchainTimestamp = dto.blockchainTimestamp,
                        createdAt = System.currentTimeMillis(),
                        syncedToServer = true
                    )
                }
                batchDao.insertBatches(batches)
                Result.success(batches.map { it.toDomain() })
            } else {
                Result.failure(Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLabTests(batchId: String): Result<List<LabTest>> {
        return try {
            val response = apiService.getLabTests(authToken, batchId)
            if (response.isSuccessful && response.body() != null) {
                val tests = response.body()!!.map { dto ->
                    LabTest(
                        id = dto.id,
                        batchId = dto.batchId,
                        testType = dto.testType,
                        testDate = dto.testDate,
                        labName = dto.labName,
                        status = dto.status,
                        isPassed = dto.isPassed,
                        resultUrl = dto.resultUrl,
                        notes = dto.notes
                    )
                }
                Result.success(tests)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun getBlockchainRecord(batchId: String): Result<BlockchainRecord?> {
        return try {
            val response = apiService.getBlockchainRecord(authToken, batchId)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val record = BlockchainRecord(
                    id = dto.id,
                    batchId = dto.batchId,
                    transactionHash = dto.transactionHash,
                    blockNumber = dto.blockNumber,
                    network = dto.network,
                    timestamp = dto.timestamp,
                    status = BlockchainStatus.valueOf(dto.status.uppercase())
                )
                Result.success(record)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    override suspend fun verifyProduct(batchNumber: String): Result<VerificationResult> {
        return try {
            val response = apiService.verifyProduct(batchNumber)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomainModel())
            } else {
                Result.failure(Exception("Verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProductBatchEntity.toDomain() = ProductBatchEntity(
        id = id, batchNumber = batchNumber, productType = productType,
        quantity = quantity, unit = unit, harvestDate = harvestDate,
        farmerId = farmerId, farmerName = farmerName,
        qualityGrade = qualityGrade, blockchainHash = blockchainHash,
        blockchainStatus = blockchainStatus, blockchainTimestamp = blockchainTimestamp,
        createdAt = createdAt, syncedToServer = syncedToServer
    )
}
