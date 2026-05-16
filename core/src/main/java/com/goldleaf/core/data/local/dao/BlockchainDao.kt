package com.goldleaf.core.data.local.dao
// core/src/main/java/com/goldleaf/core/data/local/dao/BlockchainDao.kt


import androidx.room.*
import com.goldleaf.core.data.local.BlockchainRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockchainDao {

    @Query("SELECT * FROM blockchain_records WHERE batchId = :batchId")
    suspend fun getRecordByBatch(batchId: String): BlockchainRecordEntity?

    @Query("SELECT * FROM blockchain_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: String): BlockchainRecordEntity?

    @Query("SELECT * FROM blockchain_records WHERE status = 'CONFIRMED'  ORDER BY timestamp DESC")
    fun getVerifiedRecords(): Flow<List<BlockchainRecordEntity>>

    @Query("SELECT * FROM blockchain_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<BlockchainRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BlockchainRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<BlockchainRecordEntity>)

    @Update
    suspend fun updateRecord(record: BlockchainRecordEntity)

    @Delete
    suspend fun deleteRecord(record: BlockchainRecordEntity)
}
