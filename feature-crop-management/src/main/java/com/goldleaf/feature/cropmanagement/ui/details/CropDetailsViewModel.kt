package com.goldleaf.feature.cropmanagement.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.pipeline.PipelineRepository
import com.goldleaf.core.data.dto.PipelineStageDto
import com.goldleaf.feature.cropmanagement.domain.usecase.*
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import com.goldleaf.feature.cropmanagement.data.repository.GrowthStageRepository
import com.goldleaf.feature.cropmanagement.domain.repository.MonitoringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.goldleaf.core.data.local.*

////added conversion
@HiltViewModel
class CropDetailsViewModel @Inject constructor(
    private val getCropsUseCase: GetCropsUseCase,
    private val updateCropUseCase: UpdateCropUseCase,
    private val cropRepository: CropRepository,
    private val taskRepository: TaskRepository,
    private val monitoringRepository: MonitoringRepository,
    private val growthStageRepository: GrowthStageRepository,
    private val pipelineRepository: PipelineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropDetailsUiState())
    val uiState: StateFlow<CropDetailsUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(DetailTab.OVERVIEW)
    val selectedTab: StateFlow<DetailTab> = _selectedTab.asStateFlow()

    private var currentCropId: String? = null

    fun loadCropDetails(cropId: String) {
        currentCropId = cropId
        viewModelScope.launch {
            if (_uiState.value.crop?.id != cropId) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            // Refresh pipeline stages from server
            pipelineRepository.refresh()

            try {
                getCropsUseCase().collect { crops ->
                    val crop = crops.find { it.id == cropId }
                    if (crop != null) {
                        _uiState.value = _uiState.value.copy(
                            crop = crop,
                            isLoading = false,
                            error = null
                        )
                        viewModelScope.launch {
                            loadRelatedData(cropId)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Crop not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load crop details"
                )
            }
        }
    }

    private suspend fun loadRelatedData(cropId: String) {
        try {
            // Load pipeline stages from cached repository
            _uiState.value = _uiState.value.copy(
                pipelineStages = pipelineRepository.stages.value
            )

            // Subscribe to reactive streams for tasks and monitoring
            viewModelScope.launch {
                taskRepository.getTasksFlow(cropId).collect { tasks ->
                    updateTasksInState(tasks)
                }
            }

            // ✅ FIXED: Load monitoring records directly from MonitoringRepository
            // This ensures newly created records show up in the UI
            viewModelScope.launch {
                monitoringRepository.getMonitoringRecordsByCropId(cropId)
                    .onSuccess { records ->
                        updateMonitoringRecordsInState(records)
                    }
                    .onFailure { error ->
                        android.util.Log.w("🌾 MONITORING", "⚠️ Failed to load monitoring records: ${error.message}")
                    }
            }

            viewModelScope.launch {
                cropRepository.getActivitiesByCropIdFlow(cropId).collect { activities ->
                    updateMonitoringInState(activities)
                }
            }

            // Load growth stages (doesn't change as frequently)
            val growthStages = growthStageRepository.getGrowthStagesByCropId(cropId)
                .getOrElse { emptyList() }
            
            updateGrowthStagesInState(growthStages)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to load related data"
            )
        }
    }

    private fun updateTasksInState(tasks: List<TaskEntity>) {
        val monitoringRecords = _uiState.value.monitoringRecords
        val growthStages = _uiState.value.growthStages
        val analytics = calculateAnalytics(tasks, monitoringRecords, growthStages)
        val crop = _uiState.value.crop
        val cropDetails = crop?.toCropDetails(monitoringRecords, growthStages)
        val upcomingTasks = tasks.toUpcomingTasks()
        val recentActivities = generateRecentActivities(tasks, monitoringRecords)

        _uiState.value = _uiState.value.copy(
            tasks = tasks,
            analytics = analytics,
            cropDetails = cropDetails,
            upcomingTasks = upcomingTasks,
            recentActivities = recentActivities
        )
    }

    private fun updateMonitoringInState(activities: List<CropActivity>) {
        // Convert CropActivity to CropMonitoringRecord format
        val monitoringRecords = activities.map { activity ->
            CropMonitoringRecord(
                id = activity.id,
                cropId = activity.cropId,
                recordDate = activity.date,
                healthStatus = when (activity.activityType) {
                    "PEST_CONTROL" -> HealthStatus.FAIR
                    "DISEASE_TREATMENT" -> HealthStatus.FAIR
                    "FERTILIZER", "IRRIGATION" -> HealthStatus.GOOD
                    else -> HealthStatus.GOOD
                },
                moistureLevel = null,
                pestObservations = null,
                diseaseObservations = null,
                weatherConditions = null,
                photos = emptyList(),
                notes = activity.notes,
                recordedBy = null
            )
        }

        val tasks = _uiState.value.tasks
        val growthStages = _uiState.value.growthStages
        val analytics = calculateAnalytics(tasks, monitoringRecords, growthStages)
        val crop = _uiState.value.crop
        val cropDetails = crop?.toCropDetails(monitoringRecords, growthStages)
        val recentActivities = generateRecentActivities(tasks, monitoringRecords)

        _uiState.value = _uiState.value.copy(
            monitoringRecords = monitoringRecords,
            analytics = analytics,
            cropDetails = cropDetails,
            recentActivities = recentActivities
        )
    }

    // ✅ NEW: Direct monitoring records update (not converted from CropActivity)
    private fun updateMonitoringRecordsInState(records: List<CropMonitoringRecord>) {
        val tasks = _uiState.value.tasks
        val growthStages = _uiState.value.growthStages
        val analytics = calculateAnalytics(tasks, records, growthStages)
        val crop = _uiState.value.crop
        val cropDetails = crop?.toCropDetails(records, growthStages)
        val recentActivities = generateRecentActivities(tasks, records)

        android.util.Log.d("🌾 MONITORING", "✅ Updated UI state with ${records.size} monitoring records")
        _uiState.value = _uiState.value.copy(
            monitoringRecords = records,
            analytics = analytics,
            cropDetails = cropDetails,
            recentActivities = recentActivities
        )
    }

    private fun updateGrowthStagesInState(growthStages: List<CropGrowthStage>) {
        val tasks = _uiState.value.tasks
        val monitoringRecords = _uiState.value.monitoringRecords
        val analytics = calculateAnalytics(tasks, monitoringRecords, growthStages)
        val crop = _uiState.value.crop
        val cropDetails = crop?.toCropDetails(monitoringRecords, growthStages)

        _uiState.value = _uiState.value.copy(
            growthStages = growthStages,
            analytics = analytics,
            cropDetails = cropDetails
        )
    }

    fun transitionGrowthStage(nextStageName: String) {
        val crop = _uiState.value.crop ?: return
        val currentStage = getCurrentGrowthStage()

        // This is valid here because we are inside a ViewModel
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            // Call the repository to do the heavy lifting
                val result = growthStageRepository.transitionStage(
                    cropId = crop.id,
                    currentStageId = currentStage?.id,
                    nextStageName = nextStageName,
                    transitionDate = today
                )

                result.onSuccess {
                    // Logic for auto-logging records and refreshing
                    refreshData()
                    _uiState.value = _uiState.value.copy(message = "Migrated to $nextStageName")
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addMonitoringRecord(record: CropMonitoringRecord) {
        viewModelScope.launch {
            try {
                android.util.Log.d("🌾 MONITORING", "📝 ViewModel.addMonitoringRecord() called for cropId=${record.cropId}")
                android.util.Log.d("🌾 MONITORING", "   Health: ${record.healthStatus}, Moisture: ${record.moistureLevel}%, Weather: ${record.weatherConditions}")
                
                val result = monitoringRepository.insertMonitoringRecord(record)

                result.onSuccess { savedRecord ->
                    android.util.Log.d("🌾 MONITORING", "✅ Repository insert SUCCESS: id=${savedRecord.id}")
                    currentCropId?.let { cropId ->
                        android.util.Log.d("🌾 MONITORING", "🔄 Reloading crop data for $cropId")
                        loadRelatedData(cropId)
                    }
                    _uiState.value = _uiState.value.copy(
                        message = "Monitoring record added successfully"
                    )
                }.onFailure { error ->
                    android.util.Log.e("🌾 MONITORING", "❌ Repository insert FAILED: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to add monitoring record"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("🌾 MONITORING", "💥 Exception in addMonitoringRecord: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add monitoring record"
                )
            }
        }
    }

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                // Call actual TaskRepository -> ApiService -> POST /tasks
                val result = taskRepository.addTask(task.id,task)

                result.onSuccess {
                    // Reload data after successful add
                    currentCropId?.let { loadRelatedData(it) }
                    _uiState.value = _uiState.value.copy(
                        message = "Task added successfully"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to add task"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add task"
                )
            }
        }
    }

    fun markTaskComplete(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskStatus(taskId, true)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            message = "Task marked complete"
                        )
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(error = it.message)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                // Call actual TaskRepository -> ApiService -> PUT /tasks/{taskId}
                val result = taskRepository.updateTaskStatus(taskId, isCompleted)

                result.onSuccess {
                    // Reload data after successful update
                    currentCropId?.let { loadRelatedData(it) }
                    _uiState.value = _uiState.value.copy(
                        message = "Task status updated"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update task"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update task"
                )
            }
        }
    }

    private fun calculateAnalytics(
        tasks: List<TaskEntity>,
        monitoringRecords: List<CropMonitoringRecord>,
        growthStages: List<CropGrowthStage>
    ): CropAnalytics? {
        val crop = _uiState.value.crop ?: return null

        return CropAnalytics(
            daysSincePlanting = crop.getDaysSincePlanting(),
            daysUntilHarvest = crop.getDaysUntilHarvest(),
            yieldEfficiency = crop.getYieldEfficiency(),
            completedTasksPercentage = if (tasks.isNotEmpty()) {
                (tasks.count { it.isCompleted }.toDouble() / tasks.size) * 100
            } else 0.0,
            overdueTasks = getOverdueTasksCount(tasks),
            upcomingTasks = getUpcomingTasksCount(tasks),
            averageHealthStatus = getAverageHealthStatus(monitoringRecords),
            lastMonitoringDate = monitoringRecords.maxByOrNull { it.recordDate }?.recordDate,
            currentGrowthStage = getCurrentGrowthStageName(growthStages)
        )
    }

    private fun getOverdueTasksCount(tasks: List<TaskEntity>): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return tasks.count { task ->
            !task.isCompleted && task.dueDate < today
        }
    }

    private fun getUpcomingTasksCount(tasks: List<TaskEntity>, days: Int = 7): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, days)
        val futureDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return tasks.count { task ->
            !task.isCompleted && task.dueDate >= today && task.dueDate <= futureDate
        }
    }

    private fun getAverageHealthStatus(monitoringRecords: List<CropMonitoringRecord>): HealthStatus? {
        if (monitoringRecords.isEmpty()) return null

        val recentRecords = monitoringRecords.sortedByDescending { it.recordDate }.take(5)
        val avgScore = recentRecords.map {
            when (it.healthStatus) {
                HealthStatus.EXCELLENT -> 5
                HealthStatus.GOOD -> 4
                HealthStatus.FAIR -> 3
                HealthStatus.POOR -> 2
                HealthStatus.CRITICAL -> 1
            }
        }.average()

        return when {
            avgScore >= 4.5 -> HealthStatus.EXCELLENT
            avgScore >= 3.5 -> HealthStatus.GOOD
            avgScore >= 2.5 -> HealthStatus.FAIR
            avgScore >= 1.5 -> HealthStatus.POOR
            else -> HealthStatus.CRITICAL
        }
    }

    private fun getCurrentGrowthStageName(growthStages: List<CropGrowthStage>): String? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return growthStages.find { stage ->
            stage.startDate <= today && (stage.endDate == null || stage.endDate!! >= today)
        }?.stage?.name
    }

    fun updateCropStatus(newStatus: CropStatus) {
        viewModelScope.launch {
            val currentCrop = _uiState.value.crop
            if (currentCrop != null) {
                // ✅ Validate transition
                if (currentCrop.status == null || !currentCrop.status!!.canTransitionTo(newStatus)) {
                    _uiState.value = _uiState.value.copy(
                        error = "Cannot transition from ${currentCrop.status?.getDisplayName() ?: "Unknown"} to ${newStatus.getDisplayName()}"
                    )
                    return@launch
                }

                try {
                    val updatedCrop = currentCrop.copy(
                        status = newStatus,
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .format(Date())
                    )

                    val result = updateCropUseCase(updatedCrop)
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(
                            crop = updatedCrop,
                            message = "Crop status updated to ${newStatus.getDisplayName()}"
                        )
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to update crop status"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to update crop status"
                    )
                }
            }
        }
    }

    fun updateCropField(field: CropField, value: Any) {
        viewModelScope.launch {
            val currentCrop = _uiState.value.crop
            if (currentCrop != null) {
                try {
                    val updatedCrop = when (field) {
                        CropField.NAME -> currentCrop.copy(name = value as String)
                        CropField.VARIETY -> currentCrop.copy(variety = value as String)
                        CropField.LOCATION -> currentCrop.copy(location = value as String)
                        CropField.AREA -> currentCrop.copy(area = value as Double)
                        CropField.EXPECTED_YIELD -> currentCrop.copy(expectedYield = value as Double?)
                        CropField.ACTUAL_YIELD -> currentCrop.copy(actualYield = value as Double?)
                        CropField.HARVEST_DATE -> currentCrop.copy(harvestDate = value as String?)
                        CropField.NOTES -> currentCrop.copy(notes = value as String?)
                    }.copy(
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .format(Date())
                    )

                    val result = updateCropUseCase(updatedCrop)
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(
                            crop = updatedCrop,
                            message = "Crop updated successfully"
                        )
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to update crop"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to update crop"
                    )
                }
            }
        }
    }

    fun selectTab(tab: DetailTab) {
        _selectedTab.value = tab
    }

    fun refreshData() {
        currentCropId?.let { cropId ->
            loadCropDetails(cropId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // Helper functions for calculations
    fun getDaysUntilHarvest(): Int? {
        val crop = _uiState.value.crop ?: return null
        return crop.getDaysUntilHarvest()
    }

    fun getDaysSincePlanting(): Int {
        val crop = _uiState.value.crop ?: return 0
        return crop.getDaysSincePlanting()
    }

    fun getYieldEfficiency(): Double? {
        val crop = _uiState.value.crop ?: return null
        return crop.getYieldEfficiency()
    }

    fun getCompletedTasksPercentage(): Double {
        val tasks = _uiState.value.tasks
        if (tasks.isEmpty()) return 0.0

        val completedTasks = tasks.count { it.isCompleted }
        return (completedTasks.toDouble() / tasks.size) * 100
    }

    fun getOverdueTasks(): List<TaskEntity> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return _uiState.value.tasks.filter { task ->
            !task.isCompleted && task.dueDate < today
        }
    }

    fun getUpcomingTasks(days: Int = 7): List<TaskEntity> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, days)
        val futureDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return _uiState.value.tasks.filter { task ->
            !task.isCompleted && task.dueDate >= today && task.dueDate <= futureDate
        }
    }

    fun getLatestMonitoringRecord(): CropMonitoringRecord? {
        return _uiState.value.monitoringRecords.maxByOrNull { it.recordDate }
    }

    fun getCurrentGrowthStage(): CropGrowthStage? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return _uiState.value.growthStages.find { stage ->
            stage.startDate <= today && (stage.endDate == null || stage.endDate!! >= today)
        }
    }
}


data class CropDetailsUiState(
    val crop: CropEntity? = null,
    val cropDetails: CropDetails? = null,
    val tasks: List<TaskEntity> = emptyList(),
    val monitoringRecords: List<CropMonitoringRecord> = emptyList(),
    val growthStages: List<CropGrowthStage> = emptyList(),
    val analytics: CropAnalytics? = null,
    val upcomingTasks: List<UpcomingTask> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val pipelineStages: List<PipelineStageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

// Detail Tabs
enum class DetailTab(val displayName: String) {
    OVERVIEW("Overview"),
    TASKS("Tasks"),
    MONITORING("Monitoring"),
    ANALYTICS("Analytics")
}

// Crop Fields for Updates
enum class CropField {
    NAME,
    VARIETY,
    LOCATION,
    AREA,
    EXPECTED_YIELD,
    ACTUAL_YIELD,
    HARVEST_DATE,
    NOTES
}

// Crop Analytics Data Class
data class CropAnalytics(
    val daysSincePlanting: Int,
    val daysUntilHarvest: Int?,
    val yieldEfficiency: Double?,
    val completedTasksPercentage: Double,
    val overdueTasks: Int,
    val upcomingTasks: Int,
    val averageHealthStatus: HealthStatus?,
    val lastMonitoringDate: String?,
    val currentGrowthStage: String?
)

// Detail Events
sealed class CropDetailsEvent {
    object RefreshData : CropDetailsEvent()
    data class UpdateStatus(val status: CropStatus) : CropDetailsEvent()
    data class UpdateField(val field: CropField, val value: Any) : CropDetailsEvent()
    data class AddMonitoringRecord(val record: CropMonitoringRecord) : CropDetailsEvent()
    data class AddTask(val task: TaskEntity) : CropDetailsEvent()
    data class UpdateTaskStatus(val taskId: String, val isCompleted: Boolean) : CropDetailsEvent()
    data class SelectTab(val tab: DetailTab) : CropDetailsEvent()
    object ClearError : CropDetailsEvent()
    object ClearMessage : CropDetailsEvent()
}

// CropDetailsViewModel.kt
// Extension function to convert Crop domain model to CropDetails UI model
private fun CropEntity.toCropDetails(
    monitoringRecords: List<CropMonitoringRecord>,
    growthStages: List<CropGrowthStage>
): CropDetails {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val plantingDate = try {
        dateFormat.parse(this.plantingDate) ?: Date()
    } catch (e: Exception) {
        Date()
    }

    val harvestDate = try {
        this.harvestDate?.let { dateFormat.parse(it) }
    } catch (e: Exception) {
        null
    } ?: run {
        val calendar = Calendar.getInstance()
        calendar.time = plantingDate
        calendar.add(Calendar.MONTH, 4)
        calendar.time
    }

    val today = Date()
    val daysToHarvest = ((harvestDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
    val totalDays = ((harvestDate.time - plantingDate.time) / (1000 * 60 * 60 * 24)).toInt()
    val daysElapsed = ((today.time - plantingDate.time) / (1000 * 60 * 60 * 24)).toInt()
    val progress = if (totalDays > 0) (daysElapsed * 100 / totalDays).coerceIn(0, 100) else 0

    // Get latest health status from monitoring records
    val latestMonitoring = monitoringRecords.maxByOrNull { it.recordDate }
    val healthScore = latestMonitoring?.healthStatus?.name ?: "Unknown"

    // Get current growth stage
    val currentStage = growthStages.find { stage ->
        val startDate = dateFormat.parse(stage.startDate)
        val endDate = stage.endDate?.let { dateFormat.parse(it) }
        val now = Date()
        startDate != null && startDate <= now && (endDate == null || endDate >= now)
    }

    return CropDetails(
        id = this.id,
        name = this.name,
        variety = this.variety,
        status = this.status?.name ?: CropStatus.PLANNED.name,
        statusColor = getStatusColor(this.status ?: CropStatus.PLANNED),
        growthStage = currentStage?.stage?.name ?: getGrowthStageFromProgress(progress),
        progressPercentage = progress,
        area = this.area,
        daysToHarvest = daysToHarvest.coerceAtLeast(0),
        farmName = this.location,
        plantingDate = displayFormat.format(plantingDate),
        expectedHarvestDate = displayFormat.format(harvestDate),
        soilType = this.notes ?: "Unknown", // Use notes field or add soilType to domain model
        expectedYield = this.expectedYield?.toInt() ?: 0,
        healthScore = healthScore,
        marketPrice = 0 // Will be populated from market service integration
    )
}

private fun getStatusColor(status: CropStatus): Color {
    return when (status) {
        CropStatus.PLANNED -> Color(0xFF9E9E9E)
        CropStatus.PLANTED -> Color(0xFF2196F3)
        CropStatus.GROWING -> Color(0xFF4CAF50)
        CropStatus.HARVESTED -> Color(0xFFFF9800)
        CropStatus.COMPLETED -> Color(0xFF795548)
        else -> Color(0xFFF44336)
    }
}

private fun getGrowthStageFromProgress(progress: Int): String {
    return when {
        progress < 25 -> "Germination"
        progress < 50 -> "Vegetative"
        progress < 75 -> "Flowering"
        progress < 100 -> "Maturation"
        else -> "Ready for Harvest"
    }
}

private fun List<TaskEntity>.toUpcomingTasks(): List<UpcomingTask> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    return this.filter { !it.isCompleted && it.dueDate >= today }
        .sortedBy { it.dueDate }
        .map { task ->
            val dueDate = try {
                val date = dateFormat.parse(task.dueDate)
                val days = ((date!!.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                when {
                    days == 0 -> "Today"
                    days == 1 -> "Tomorrow"
                    days < 7 -> "In $days days"
                    else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                task.dueDate
            }

            UpcomingTask(
                name = task.title,
                description = task.description ?: "",
                dueDate = dueDate,
                icon = getTaskIcon(task.category.toString()),
                color = getTaskColor(task.priority.toString())
            )
        }
}

private fun generateRecentActivities(
    tasks: List<TaskEntity>,
    monitoringRecords: List<CropMonitoringRecord>
): List<RecentActivity> {
    val activities = mutableListOf<RecentActivity>()

    // Add completed tasks
    tasks.filter { it.isCompleted && it.completedDate != null }
        .sortedByDescending { it.completedDate }
        .forEach { task ->
            activities.add(
                RecentActivity(
                    name = task.title,
                    description = task.description ?: "Task completed",
                    date = formatRelativeDate(task.completedDate!!),
                    icon = getTaskIcon(task.category.toString()),
                    color = Color(0xFF4CAF50)
                )
            )
        }

    // Add monitoring records
    monitoringRecords.sortedByDescending { it.recordDate }
        .forEach { record ->
            activities.add(
                RecentActivity(
                    name = "Health Check",
                    description = "Status: ${record.healthStatus.name}${record.notes?.let { " - $it" } ?: ""}",
                    date = formatRelativeDate(record.recordDate),
                    icon = Icons.Default.Visibility,
                    color = getHealthColor(record.healthStatus)
                )
            )
        }

    return activities.sortedByDescending { it.date }
}

private fun getTaskIcon(taskType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (taskType.lowercase()) {
        "fertilization" -> Icons.Default.Science
        "pest_control" -> Icons.Default.BugReport
        "irrigation" -> Icons.Default.WaterDrop
        "weeding" -> Icons.Default.Grass
        "harvesting" -> Icons.Default.Agriculture
        "planting" -> Icons.Default.Yard
        else -> Icons.Default.Task
    }
}

private fun getTaskColor(priority: String): Color {
    return when (priority.lowercase()) {
        "high" -> Color(0xFFFF5252)
        "medium" -> Color(0xFFFF9800)
        "low" -> Color(0xFF4CAF50)
        else -> Color(0xFF2196F3)
    }
}

private fun getHealthColor(healthStatus: HealthStatus): Color {
    return when (healthStatus) {
        HealthStatus.EXCELLENT -> Color(0xFF4CAF50)
        HealthStatus.GOOD -> Color(0xFF8BC34A)
        HealthStatus.FAIR -> Color(0xFFFF9800)
        HealthStatus.POOR -> Color(0xFFFF5722)
        HealthStatus.CRITICAL -> Color(0xFFF44336)
    }
}

private fun formatRelativeDate(dateString: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        val days = ((Date().time - date!!.time) / (1000 * 60 * 60 * 24)).toInt()
        when {
            days == 0 -> "Today"
            days == 1 -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        dateString
    }
}