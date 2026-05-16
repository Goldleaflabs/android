package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.ProductBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductBatchDao  {

    @Query("SELECT * FROM product_batches WHERE farmId = :farmId ORDER BY createdAt DESC")
    fun getBatchesByFarmer(farmId: String): Flow<List<ProductBatchEntity>>

    @Query("SELECT * FROM product_batches WHERE id = :batchId")
     fun getBatchById(batchId: String): Flow<ProductBatchEntity?>

    @Query("SELECT * FROM product_batches WHERE batchNumber = :batchNumber")
     fun getBatchByNumber(batchNumber: String): Flow<ProductBatchEntity?>

    @Query("SELECT * FROM product_batches WHERE status = :status ORDER BY createdAt DESC")
    fun getBatchesByStatus(status: String): Flow<List<ProductBatchEntity>>

    @Query("SELECT * FROM product_batches WHERE blockchainRecordId IS NOT NULL")
    fun getVerifiedBatches(): Flow<List<ProductBatchEntity>>

    @Query("SELECT * FROM product_batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<ProductBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ProductBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<ProductBatchEntity>)

    @Update
    suspend fun updateBatch(batch: ProductBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: ProductBatchEntity)

    @Query("DELETE FROM product_batches WHERE farmId = :farmId")
    suspend fun deleteBatchesByFarm(farmId: String)
}
