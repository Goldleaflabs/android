package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.QualityParameterEntity

@Dao
interface QualityDao {
    @Query("SELECT * FROM quality_parameters WHERE batchId = :batchId")
    suspend fun getQualityParametersByBatchId(batchId: String): List<QualityParameterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qualityParameter: QualityParameterEntity)
}
