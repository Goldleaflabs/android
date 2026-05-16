package com.goldleaf.core.data.local.dao


import androidx.room.*
import com.goldleaf.core.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Get tasks by cropId only
    @Query("SELECT * FROM tasks WHERE cropId = :cropId")
    fun getTasksByCropId(cropId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE cropId = :cropId")
    suspend fun getTasksByCropIdSync(cropId: String): List<TaskEntity>

    // ✅ Get tasks by farmId
    @Query("SELECT * FROM tasks WHERE farmId = :farmId")
    fun getTasksByFarmId(farmId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE farmId = :farmId")
    suspend fun getTasksByFarmIdSync(farmId: String): List<TaskEntity>

    // ✅ Get tasks by both farmId and cropId (most specific)
    @Query("SELECT * FROM tasks WHERE farmId = :farmId AND cropId = :cropId")
    fun getTasksByFarmAndCrop(farmId: String, cropId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE farmId = :farmId AND cropId = :cropId")
    suspend fun getTasksByFarmAndCropSync(farmId: String, cropId: String): List<TaskEntity>

    // Get all tasks
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM tasks WHERE farmId = :farmId")
    suspend fun deleteTasksByFarmId(farmId: String)

    @Query("DELETE FROM tasks WHERE cropId = :cropId")
    suspend fun deleteTasksByCropId(cropId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}