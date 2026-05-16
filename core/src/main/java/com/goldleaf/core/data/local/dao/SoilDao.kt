package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.SoilTestEntity

@Dao
interface SoilDao {
    @Query("SELECT * FROM soil_tests WHERE farmId = :farmId ORDER BY testDate DESC")
    suspend fun getSoilTestsByFarmId(farmId: String): List<SoilTestEntity>

    @Query("SELECT * FROM soil_tests WHERE farmId = :farmId ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatestSoilTest(farmId: String): SoilTestEntity?

    @Query("SELECT * FROM soil_tests WHERE id = :testId")
    suspend fun getSoilTestById(testId: String): SoilTestEntity?

    @Query("""
        SELECT * FROM soil_tests 
        WHERE farmId = :farmId 
        AND testDate >= :startDate 
        ORDER BY testDate DESC
    """)
    suspend fun getRecentSoilTests(farmId: String, startDate: Long): List<SoilTestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoilTest(soilTest: SoilTestEntity)

    @Update
    suspend fun updateSoilTest(soilTest: SoilTestEntity)

    @Delete
    suspend fun deleteSoilTest(soilTest: SoilTestEntity)

    @Query("DELETE FROM soil_tests WHERE testDate < :cutoffDate")
    suspend fun deleteOldSoilTests(cutoffDate: Long)

    @Query("""
        SELECT AVG(ph) as avgPh, 
               AVG(nitrogen) as avgN, 
               AVG(phosphorus) as avgP, 
               AVG(potassium) as avgK 
        FROM soil_tests 
        WHERE farmId = :farmId 
        AND testDate >= :startDate
    """)
    suspend fun getAverageSoilMetrics(farmId: String, startDate: Long): SoilMetricsAverage?
}

data class SoilMetricsAverage(
    val avgPh: Double?,
    val avgN: Double?,
    val avgP: Double?,
    val avgK: Double?
)
