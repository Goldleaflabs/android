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
import com.goldleaf.core.util.onSuccess
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userSession: UserSessionManager,
    private val farmerRepository: FarmerRepository,
    private val tasksRepository: TaskRepository,
    private val cropDao: CropDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _selectedFarmName = MutableStateFlow<String?>(null)

    // --- REACTIVE FARMER PROFILE ---
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


    // ✅ UPDATED: Now accepts farmerId as parameter
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
                    it.copy(
                        error = e.message
                    )
                }
            }
        }
    }


    private var dataCollectionJob: Job? = null

    fun loadDashboardData(farmerId: String, selectedFarmId: String) {
        // Cancel previous collection if any
        dataCollectionJob?.cancel()

        dataCollectionJob = viewModelScope.launch {
            // Collect farms Flow - updates automatically when data changes
            farmerRepository.getFarmerFarms(farmerId)
                .collectLatest { allFarms ->

                    if (allFarms.isEmpty()) {
                        _uiState.update { it.copy(totalFarms = 0) }
                        return@collectLatest
                    }

                    // Set farm name
                    _selectedFarmName.value = if (selectedFarmId == "all" || selectedFarmId.isEmpty()) {
                        "All Farms"
                    } else {
                        allFarms.find { it.id == selectedFarmId }?.name
                    }

                    // Process and update dashboard reactively
                    processAndUpdateDashboard(allFarms)
                }
        }
    }

    private suspend fun processAndUpdateDashboard(allFarms: List<Farm>) {
        // Create flows for each farm's data
        val farmDataFlows = allFarms.map { farm ->
            combine(
                // Get crops as Flow - convert Result to Flow
                flow {
                    farmerRepository.getFarmCrops(farm.id).onSuccess { crops ->
                        emit(crops)
                    }
                }.catch { emit(emptyList()) },
                // Get tasks as Flow - convert Result to Flow
                flow {
                    tasksRepository.getTasksByFarmId(farm.id).onSuccess { tasks ->
                        emit(tasks)
                    }
                }.catch { emit(emptyList()) }
            ) { crops, tasks ->
                // For each crop, get its activities from the DAO (which returns Flow)
                val activities = crops.flatMap { crop ->
                    cropDao.getActivitiesByCropId(crop.id).firstOrNull() ?: emptyList()
                }
                Triple(crops, tasks, activities)
            }
        }

        // Combine all farm data into a single flow
        combine(farmDataFlows) { farmDataArray ->
            val allCrops = mutableListOf<CropEntity>()
            val allTasks = mutableListOf<TaskEntity>()
            val allActivities = mutableListOf<CropActivity>()

            farmDataArray.forEach { (crops, tasks, activities) ->
                allCrops.addAll(crops)
                allTasks.addAll(tasks)
                allActivities.addAll(activities)
            }

            Triple(allCrops, allTasks, allActivities)
        }.collect { (crops, tasks, activities) ->
            // Silently update UI whenever any data changes
            _uiState.update { currentState ->
                currentState.copy(
                    farms = allFarms,
                    totalFarms = allFarms.size,
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


    override fun onCleared() {
        super.onCleared()
        dataCollectionJob?.cancel()
    }


    // ✅ UPDATED: Now accepts farmerId as parameter
    fun refreshDashboard(farmerId: String, selectedFarmId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            if (selectedFarmId.isNotEmpty()) {
                loadDashboardData(farmerId, selectedFarmId)
            } else {
                loadAllFarms(farmerId)
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // ✅ UPDATED: Now accepts both parameters
    fun onScreenLoaded(farmerId: String, selectedFarmId: String) {
        if (selectedFarmId.isNotEmpty()) {
            loadDashboardData(farmerId, selectedFarmId)
        } else {
            loadAllFarms(farmerId)
        }
    }

    // Load farmer profile info from session
    fun loadFarmerProfile() {
        viewModelScope.launch {
            try {
                userSession.getCurrentUserId()
                // Profile data flows through currentFarmer state
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    }


data class DashboardUiState(
    val farms: List<Farm> = emptyList(),
    val totalFarms: Int = 0,
    val activeCrops: Int = 0,
    val pendingTasks: Int = 0,
    val recentActivities: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)