package com.goldleaf.core.data.local.dao


// core/src/main/java/com/goldleaf/core/data/local/dao/QualityParameterDao.kt


import androidx.room.*
import com.goldleaf.core.data.local.QualityParameterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QualityParameterDao {

    @Query("SELECT * FROM quality_parameters WHERE batchId = :batchId")
    fun getParametersByBatch(batchId: String): Flow<List<QualityParameterEntity>>

    @Query("SELECT * FROM quality_parameters WHERE batchId = :batchId AND status = 'FAIL' ")
    fun getFailedParameters(batchId: String): Flow<List<QualityParameterEntity>>

    @Query("SELECT * FROM quality_parameters WHERE id = :parameterId")
    suspend fun getParameterById(parameterId: String): QualityParameterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameter(parameter: QualityParameterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(parameters: List<QualityParameterEntity>)

    @Update
    suspend fun updateParameter(parameter: QualityParameterEntity)

    @Delete
    suspend fun deleteParameter(parameter: QualityParameterEntity)

    @Query("DELETE FROM quality_parameters WHERE batchId = :batchId")
    suspend fun deleteParametersByBatch(batchId: String)
}
