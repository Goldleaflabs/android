package com.goldleaf.core.data.local.dao

// core/src/main/java/com/goldleaf/core/data/local/dao/AuditRecordDao.kt

import androidx.room.*
import com.goldleaf.core.data.local.AuditRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditRecordDao {
/*
    @Query("SELECT * FROM audit_records WHERE certificationId = :certificationId ORDER BY auditDate DESC")
    fun getAuditsByCertification(certificationId: String): Flow<List<AuditRecordEntity>>
*/
    @Query("SELECT * FROM audit_records WHERE id = :auditId")
    suspend fun getAuditById(auditId: String): AuditRecordEntity?

    @Query("SELECT * FROM audit_records ORDER BY auditDate DESC LIMIT :limit")
    fun getRecentAudits(limit: Int = 10): Flow<List<AuditRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudits(audits: List<AuditRecordEntity>)

    @Update
    suspend fun updateAudit(audit: AuditRecordEntity)

    @Delete
    suspend fun deleteAudit(audit: AuditRecordEntity)
}
