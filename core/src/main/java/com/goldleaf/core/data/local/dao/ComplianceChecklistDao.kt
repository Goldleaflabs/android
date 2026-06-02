package com.goldleaf.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.goldleaf.core.data.local.ComplianceChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplianceChecklistDao {
    @Query("SELECT * FROM compliance_checklist WHERE farmId = :farmId ORDER BY category ASC, dueDate ASC")
    fun getItemsByFarm(farmId: String): Flow<List<ComplianceChecklistEntity>>

    @Query("SELECT * FROM compliance_checklist WHERE farmId = :farmId AND category = :category ORDER BY dueDate ASC")
    fun getItemsByFarmAndCategory(farmId: String, category: String): Flow<List<ComplianceChecklistEntity>>

    @Query("SELECT * FROM compliance_checklist WHERE farmerId = :farmerId ORDER BY category ASC")
    fun getItemsByFarmer(farmerId: String): Flow<List<ComplianceChecklistEntity>>

    @Query("SELECT * FROM compliance_checklist WHERE id = :id")
    suspend fun getItemById(id: String): ComplianceChecklistEntity?

    @Query("SELECT DISTINCT category FROM compliance_checklist WHERE farmId = :farmId")
    fun getCategoriesByFarm(farmId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ComplianceChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ComplianceChecklistEntity>)

    @Update
    suspend fun updateItem(item: ComplianceChecklistEntity)

    @Delete
    suspend fun deleteItem(item: ComplianceChecklistEntity)

    @Query("DELETE FROM compliance_checklist WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM compliance_checklist WHERE farmId = :farmId")
    suspend fun deleteItemsByFarm(farmId: String)
}
