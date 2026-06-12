package com.goldleaf.feature.farmermanagement.ui.viewmodels

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.dto.farm.Farm
import com.goldleaf.core.data.dto.farm.FarmerDashboardData
import com.goldleaf.core.data.local.FarmerEntity
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import com.goldleaf.feature.farmermanagement.ui.dashboard.DashboardFarmer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Define it here so it's scoped only to this screen
data class ListFarmer(
    val id: String,
    val name: String
)

@HiltViewModel
class FarmSelectionViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val userSession: UserSessionManager,
    private val cropDao: CropDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmSelectionUiState())
    val uiState: StateFlow<FarmSelectionUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFarmer: StateFlow<ListFarmer?> = combine(
        userSession.currentFarmer,
        userSession.userRole
    ) { entity, _ ->
        entity?.let {
            ListFarmer(
                id = it.id,
                name = it.name
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )


    init {
        viewModelScope.launch {
            currentFarmer.collect { farmer ->
                if (farmer != null) {
                    if (!farmer.id.isNullOrBlank()) {
                        // Sync all data from server before reading local
                        withContext(Dispatchers.IO) {
                            try { farmerRepository.syncAllFromServer(farmer.id) } catch (_: Exception) { }
                            try { farmerRepository.syncFarmerData() } catch (_: Exception) { }
                        }
                        loadFarms(farmer.id)
                        loadRecentActivities()
                    }
                }
            }
        }
    }

    private fun loadRecentActivities() {
        viewModelScope.launch {
            try {
                val farmerId = currentFarmer.value?.id ?: return@launch
                val activities = cropDao.getActivitiesByFarmerId(farmerId).first()
                    .map { it.description }
                _uiState.update { it.copy(recentActivities = activities) }
            } catch (e: Exception) {
                Log.w("FarmSelectionVM", "Failed to load recent activities", e)
            }
        }
    }

    fun loadFarms(farmerId: String) {
        if (farmerId.isBlank()) {
            Log.e("FarmSelectionViewModel", "❌ loadFarms called with blank farmerId")
            _uiState.update { it.copy(error = "Invalid farmer ID") }
            return
        }

        Log.d("FarmSelectionViewModel", "📥 loadFarms: Starting to load farms for farmerId='$farmerId'")
        viewModelScope.launch {
            // Only show loading if we don't have data yet (local-first approach)
            if (_uiState.value.farms.isEmpty()) {
                Log.d("FarmSelectionViewModel", "📥 loadFarms: No local data, setting isLoading=true")
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                Log.d("FarmSelectionViewModel", "📥 loadFarms: Collecting farms from repository...")
                farmerRepository.getFarmerFarms(farmerId)
                    .catch { e ->
                        Log.e("FarmSelectionViewModel", "❌ loadFarms: Error collecting farms", e)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load farms"
                            )
                        }
                    }
                    .collectLatest { farms ->
                        Log.d("FarmSelectionViewModel", "📥 loadFarms: Received ${farms.size} farms, updating UI")
                        _uiState.update {
                            it.copy(
                                farms = farms,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("FarmSelectionViewModel", "❌ loadFarms: Unexpected exception", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun refreshFarms(farmerId: String) {
        Log.d("FarmSelectionViewModel", "🔄 refreshFarms: Starting refresh for farmerId='$farmerId'")
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            withContext(Dispatchers.IO) {
                try { farmerRepository.syncAllFromServer(farmerId) } catch (_: Exception) { }
            }
            loadFarms(farmerId)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}

data class FarmSelectionUiState(
    val farms: List<Farm> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val recentActivities: List<String> = emptyList()
)