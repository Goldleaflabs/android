package com.goldleaf.core.data.local.dao

// core/src/main/java/com/goldleaf/core/data/local/dao/CertificationRequirementDao.kt

import androidx.room.*
import com.goldleaf.core.data.local.CertificationRequirementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificationRequirementDao {

    @Query("SELECT * FROM certification_requirements WHERE certificationId = :certificationId")
    fun getRequirementsByCertification(certificationId: String): Flow<List<CertificationRequirementEntity>>

    @Query("SELECT * FROM certification_requirements WHERE certificationId = :certificationId AND Status ='MET'")
    fun getUnmetRequirements(certificationId: String): Flow<List<CertificationRequirementEntity>>

    @Query("SELECT * FROM certification_requirements WHERE id = :requirementId")
    suspend fun getRequirementById(requirementId: String): CertificationRequirementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequirement(requirement: CertificationRequirementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequirements(requirements: List<CertificationRequirementEntity>)

    @Update
    suspend fun updateRequirement(requirement: CertificationRequirementEntity)

    @Delete
    suspend fun deleteRequirement(requirement: CertificationRequirementEntity)

    @Query("DELETE FROM certification_requirements WHERE certificationId = :certificationId")
    suspend fun deleteRequirementsByCertification(certificationId: String)
}
