package com.goldleaf.feature.farmermanagement.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.local.CropActivity
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.FarmEntity
import com.goldleaf.core.data.local.TaskEntity
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.core.data.local.dao.PlotDao
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userSession: UserSessionManager,
    private val farmerRepository: FarmerRepository,
    private val tasksRepository: TaskRepository,
    private val cropDao: CropDao,
    private val plotDao: PlotDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _selectedFarmName = MutableStateFlow<String?>(null)

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val currentFarmer: StateFlow<DashboardFarmer?> = combine(
        userSession.currentFarmer,
        userSession.userRole,
        _selectedFarmName
    ) { entity, role, farmName ->
        entity?.let {
            DashboardFarmer(
                id = it.id,
                name = it.name,
                userRole = role,
                farmname = farmName
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun loadAllFarms(farmerId: String) {
        viewModelScope.launch {
            try {
                farmerRepository.getFarmerFarms(farmerId).collectLatest { farms ->
                    _uiState.update {
                        it.copy(
                            farms = farms,
                            totalFarms = farms.size,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    private var dashboardJob: Job? = null

    fun loadDashboardData(farmerId: String, selectedFarmId: String) {
        dashboardJob?.cancel()

        dashboardJob = viewModelScope.launch {
            farmerRepository.getFarmerFarms(farmerId)
                .collectLatest { allFarms ->
                    if (allFarms.isEmpty()) {
                        _uiState.update { it.copy(totalFarms = 0) }
                        return@collectLatest
                    }

                    _selectedFarmName.value = if (selectedFarmId == "all" || selectedFarmId.isEmpty()) {
                        "All Farms"
                    } else {
                        allFarms.find { it.id == selectedFarmId }?.name
                    }

                    observeFarmData(allFarms, selectedFarmId)
                }
        }
    }

    private fun observeFarmData(allFarms: List<Farm>, selectedFarmId: String = "all") {
        val targetFarms = if (selectedFarmId == "all" || selectedFarmId.isEmpty()) {
            allFarms
        } else {
            allFarms.filter { it.id == selectedFarmId }
        }

        viewModelScope.launch {
            val farmDataFlows = targetFarms.map { farm ->
                combine(
                    farmerRepository.getFarmCropsFlow(farm.id),
                    tasksRepository.getTasksByFarmIdFlow(farm.id)
                ) { crops, tasks ->
                    val activities = crops.flatMap { crop ->
                        cropDao.getActivitiesByCropId(crop.id).firstOrNull() ?: emptyList()
                    }
                    FarmData(crops, tasks, activities)
                }
            }

            combine(farmDataFlows) { dataArray ->
                val allCrops = mutableListOf<CropEntity>()
                val allTasks = mutableListOf<TaskEntity>()
                val allActivities = mutableListOf<CropActivity>()

                dataArray.forEach { data ->
                    allCrops.addAll(data.crops)
                    allTasks.addAll(data.tasks)
                    allActivities.addAll(data.activities)
                }

                AggregatedData(allCrops, allTasks, allActivities)
            }.collect { (crops, tasks, activities) ->
                // Load plot count — sum plots across target farms
                var plotCount = 0
                try { for (f in targetFarms) plotCount += plotDao.getPlotsByFarmId(f.id).size } catch (_: Exception) {}

                _uiState.update { currentState ->
                    currentState.copy(
                        farms = allFarms,
                        totalFarms = allFarms.size,
                        totalPlots = plotCount,
                        activeCrops = crops.size,
                        pendingTasks = tasks.size,
                        recentActivities = activities
                            .sortedByDescending { it.createdAt }
                            .take(10)
                            .map { it.description }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dashboardJob?.cancel()
    }

    fun refreshDashboard(farmerId: String, selectedFarmId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            withContext(Dispatchers.IO) {
                try { farmerRepository.syncFarmerData() } catch (_: Exception) { }
                try { farmerRepository.getFarmCrops(selectedFarmId) } catch (_: Exception) { }
                try { tasksRepository.getTasksByFarmId(selectedFarmId) } catch (_: Exception) { }
            }

            delay(500)

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onScreenLoaded(farmerId: String, selectedFarmId: String) {
        if (selectedFarmId.isNotEmpty()) {
            loadDashboardData(farmerId, selectedFarmId)
        } else {
            loadAllFarms(farmerId)
        }
    }

    fun loadFarmerProfile() {
        viewModelScope.launch {
            try {
                userSession.getCurrentUserId()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }
}

private data class FarmData(
    val crops: List<CropEntity>,
    val tasks: List<TaskEntity>,
    val activities: List<CropActivity>
)

private data class AggregatedData(
    val crops: List<CropEntity>,
    val tasks: List<TaskEntity>,
    val activities: List<CropActivity>
)

data class DashboardUiState(
    val farms: List<Farm> = emptyList(),
    val totalFarms: Int = 0,
    val totalPlots: Int = 0,
    val activeCrops: Int = 0,
    val pendingTasks: Int = 0,
    val recentActivities: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
