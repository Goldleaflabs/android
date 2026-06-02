package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.InputEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InputDao {
    @Query("SELECT * FROM crop_inputs WHERE cropId = :cropId ORDER BY applicationDate DESC")
    fun getInputsByCrop(cropId: String): Flow<List<InputEntity>>

    @Query("SELECT * FROM crop_inputs WHERE farmId = :farmId ORDER BY applicationDate DESC")
    fun getInputsByFarm(farmId: String): Flow<List<InputEntity>>

    @Query("SELECT * FROM crop_inputs WHERE id = :id")
    suspend fun getInputById(id: String): InputEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInput(input: InputEntity)

    @Update
    suspend fun updateInput(input: InputEntity)

    @Delete
    suspend fun deleteInput(input: InputEntity)
}
