package com.goldleaf.certification.data.local.dao


import androidx.room.*
import com.goldleaf.core.data.local.ProductBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductBatchDao {

    @Query("SELECT * FROM product_batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<ProductBatchEntity>>

    @Query("SELECT * FROM product_batches WHERE id = :batchId")
    fun getBatchById(batchId: String): Flow<ProductBatchEntity?>

    @Query("SELECT * FROM product_batches WHERE batchNumber = :batchNumber LIMIT 1")
    fun getBatchByNumber(batchNumber: String): Flow<ProductBatchEntity?>

    @Query("SELECT * FROM product_batches WHERE farmId = :farmId")
    fun getBatchesByFarmer(farmId: String): Flow<List<ProductBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ProductBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<ProductBatchEntity>)

    @Update
    suspend fun updateBatch(batch: ProductBatchEntity)

    @Query("DELETE FROM product_batches WHERE id = :batchId")
    suspend fun deleteBatch(batchId: String)
}