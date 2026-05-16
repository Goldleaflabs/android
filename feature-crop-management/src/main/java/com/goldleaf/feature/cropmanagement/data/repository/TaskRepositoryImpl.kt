package com.goldleaf.feature.cropmanagement.data.repository

import android.util.Log
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.dao.TaskDao
import com.goldleaf.core.data.local.TaskEntity
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val taskDao: TaskDao
) : TaskRepository {

    // Get tasks by cropId (across all farms with this crop)
    override suspend fun getTasksByCropId(cropId: String): Result<List<TaskEntity>> {
        return try {
            val response = apiService.getTasksByCropId(cropId)

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!
                taskDao.insertTasks(tasks)
                Result.success(tasks)
            } else {
                val localTasks = taskDao.getTasksByCropIdSync(cropId)
                Result.success(localTasks)
            }
        } catch (e: Exception) {
            try {
                val localTasks = taskDao.getTasksByCropIdSync(cropId)
                Result.success(localTasks)
            } catch (localError: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ Get tasks by farmId (all tasks for a specific farm)
     override suspend fun getTasksByFarmId(farmId: String): Result<List<TaskEntity>> {
        return try {
            val response = apiService.getTasksByFarmId(farmId)

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!
                taskDao.insertTasks(tasks)
                Result.success(tasks)
            } else {
                val localTasks = taskDao.getTasksByFarmIdSync(farmId)
                Result.success(localTasks)
            }
        } catch (e: Exception) {
            try {
                val localTasks = taskDao.getTasksByFarmIdSync(farmId)
                Result.success(localTasks)
            } catch (localError: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ Get tasks by both farmId and cropId (most specific)
     suspend fun getTasksByFarmAndCrop(farmId: String, cropId: String): Result<List<TaskEntity>> {
        return try {
            val response = apiService.getTasksByFarmAndCrop(farmId, cropId)

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!
                taskDao.insertTasks(tasks)
                Result.success(tasks)
            } else {
                val localTasks = taskDao.getTasksByFarmAndCropSync(farmId, cropId)
                Result.success(localTasks)
            }
        } catch (e: Exception) {
            try {
                val localTasks = taskDao.getTasksByFarmAndCropSync(farmId, cropId)
                Result.success(localTasks)
            } catch (localError: Exception) {
                Result.failure(e)
            }
        }
    }

     override suspend fun addTask(taskId: String,task: TaskEntity): Result<TaskEntity> {
        return try {
            val response = apiService.createTask(taskId,task)
            if (response.isSuccessful && response.body() != null) {
                val createdTask = response.body()!!
                taskDao.insertTask(createdTask)
                Result.success(createdTask)
            } else {
                taskDao.insertTask(task)
                Result.success(task)
            }
        } catch (e: Exception) {
            taskDao.insertTask(task)
            Result.success(task)
        }
    }

     override suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            // 1. Update locally FIRST
            taskDao.updateTaskStatus(taskId, isCompleted)

            // 2. Try to sync to server
            try {
                apiService.updateTaskStatus(taskId, isCompleted)
            } catch (e: Exception) {
                // Network error - update is safe locally
                // Will retry sync when online
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            // 1. Delete locally FIRST
            taskDao.deleteTaskById(taskId)

            // 2. Try to sync deletion to server
            try {
                apiService.deleteTask(taskId)
            } catch (e: Exception) {
                // Network error - deletion is safe locally
                // Will retry sync when online
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTasksFlow(cropId: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksByCropId(cropId)
    }

    // ✅ Flow for farm-specific tasks - improved with local-first approach
    override fun getTasksByFarmIdFlow(farmId: String): Flow<List<TaskEntity>> {
        return flow {
            // 1. FIRST: Emit local data immediately
            taskDao.getTasksByFarmId(farmId)
                .collect { localTasks ->
                    emit(localTasks)
                }
            
            // 2. THEN: Refresh from remote source in background
            refreshTasksFromServer(farmId)
        }
    }

    private suspend fun refreshTasksFromServer(farmId: String) {
        try {
            val response = apiService.getTasksByFarmId(farmId)

            if (response.isSuccessful) {
                val taskDtos = response.body()
                if (!taskDtos.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) {
                        Log.d("TaskRepository", "Saving ${taskDtos.size} tasks to Room")
                        taskDao.insertTasks(taskDtos)
                    }
                }
            } else {
                Log.e("TaskRepository", "Task Sync Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Task Sync Failed", e)
        }
    }

    // ✅ Flow for farm-specific tasks
    fun getTasksFlowByFarm(farmId: String): Flow<List<TaskEntity>> {
        return getTasksByFarmIdFlow(farmId)
    }
}