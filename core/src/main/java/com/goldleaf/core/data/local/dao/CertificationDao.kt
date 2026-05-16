package com.goldleaf.core.data.local.dao

// core/src/main/java/com/goldleaf/core/data/local/dao/CertificationDao.kt


import androidx.room.*
import com.goldleaf.core.data.local.CertificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificationDao {

    @Query("SELECT * FROM certifications WHERE farmerId = :farmerId ORDER BY validFrom DESC")
    fun getCertificationsByFarmer(farmerId: String): Flow<List<CertificationEntity>>

    @Query("SELECT * FROM certifications WHERE id = :certificationId")
    suspend fun getCertificationById(certificationId: String): CertificationEntity?

    @Query("SELECT * FROM certifications WHERE status = :status")
    fun getCertificationsByStatus(status: String): Flow<List<CertificationEntity>>

    @Query("SELECT * FROM certifications WHERE validUntil < :currentTime AND status = 'ACTIVE'")
    fun getExpiredCertifications(currentTime: Long): Flow<List<CertificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertification(certification: CertificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertifications(certifications: List<CertificationEntity>)

    @Update
    suspend fun updateCertification(certification: CertificationEntity)

    @Delete
    suspend fun deleteCertification(certification: CertificationEntity)

    @Query("DELETE FROM certifications WHERE farmerId = :farmerId")
    suspend fun deleteCertificationsByFarmer(farmerId: String)


    @Query("SELECT * FROM certifications")
    fun getAllCertifications(): Flow<List<CertificationEntity>>

    @Query("DELETE FROM certifications WHERE id = :certificationId")
    suspend fun deleteCertificationById(certificationId: String)

}


