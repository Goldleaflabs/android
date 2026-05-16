package com.goldleaf.core.data.local.dao


import androidx.room.*
import com.goldleaf.core.data.local.CropTaskEntity
import com.goldleaf.core.data.local.TaskCategory
import com.goldleaf.core.data.local.TaskPriority
import kotlinx.coroutines.flow.Flow

@Dao
interface CropTaskDao {

    @Query("SELECT * FROM crop_tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<CropTaskEntity>>

    @Query("SELECT * FROM crop_tasks WHERE cropId = :cropId")
    fun getTasksByCropId(cropId: String): Flow<List<CropTaskEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CropTaskEntity)

    @Update
    suspend fun updateTask(task: CropTaskEntity)

    @Delete
    suspend fun deleteTask(task: CropTaskEntity)
}