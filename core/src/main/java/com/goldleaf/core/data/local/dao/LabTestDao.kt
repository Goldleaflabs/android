package com.goldleaf.core.data.local.dao

// core/src/main/java/com/goldleaf/core/data/local/dao/LabTestDao.kt


import androidx.room.*
import com.goldleaf.core.data.local.LabTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabTestDao {

    @Query("SELECT * FROM lab_tests WHERE batchId = :batchId ORDER BY testDate DESC")
    fun getTestsByBatch(batchId: String): Flow<List<LabTestEntity>>

    @Query("SELECT * FROM lab_tests ORDER BY testDate DESC")
    fun getAllTests(): Flow<List<LabTestEntity>>

    @Query("SELECT * FROM lab_tests WHERE id = :testId")
    suspend fun getTestById(testId: String): LabTestEntity?

    @Query("SELECT * FROM lab_tests WHERE batchId = :batchId AND status='FAILED' ")
    fun getFailedTests(batchId: String): Flow<List<LabTestEntity>>

    @Query("SELECT * FROM lab_tests WHERE testType = :testType ORDER BY testDate DESC")
    fun getTestsByType(testType: String): Flow<List<LabTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: LabTestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<LabTestEntity>)

    @Update
    suspend fun updateTest(test: LabTestEntity)

    @Delete
    suspend fun deleteTest(test: LabTestEntity)

    @Query("DELETE FROM lab_tests WHERE batchId = :batchId")
    suspend fun deleteTestsByBatch(batchId: String)
}