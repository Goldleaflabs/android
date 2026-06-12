package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.AdvisoryEntity

@Dao
interface AdvisoryDao {
    @Query("SELECT * FROM advisories WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    suspend fun getAdvisoriesByFarmerId(farmerId: String): List<AdvisoryEntity>

    @Query("SELECT * FROM advisories WHERE id = :advisoryId")
    suspend fun getAdvisoryById(advisoryId: String): AdvisoryEntity?

    @Query("UPDATE advisories SET isRead = 1 WHERE id = :advisoryId")
    suspend fun markAsRead(advisoryId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisory(advisory: AdvisoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisories(advisories: List<AdvisoryEntity>)

    @Delete
    suspend fun deleteAdvisory(advisory: AdvisoryEntity)
}
