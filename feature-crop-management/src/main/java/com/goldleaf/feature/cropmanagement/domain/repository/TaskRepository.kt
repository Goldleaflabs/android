package com.goldleaf.feature.cropmanagement.domain.repository


import com.goldleaf.core.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun getTasksByCropId(cropId: String): Result<List<TaskEntity>>
    suspend fun getTasksByFarmId(farmId: String): Result<List<TaskEntity>>
    suspend fun addTask(taskId: String,task: TaskEntity): Result<TaskEntity>
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    fun getTasksFlow(cropId: String): Flow<List<TaskEntity>>
    fun getTasksByFarmIdFlow(farmId: String): Flow<List<TaskEntity>>
}