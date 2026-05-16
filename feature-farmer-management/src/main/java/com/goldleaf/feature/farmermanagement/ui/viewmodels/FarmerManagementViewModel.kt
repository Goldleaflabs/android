
// =====================================================
// File: FarmerManagementViewModel.kt
// Location: feature-farmer-management/src/main/kotlin/com/goldleaf/feature/farmermanagement/ui/viewmodels/FarmerManagementViewModel.kt
// =====================================================
package com.goldleaf.feature.farmermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.util.Result
import com.goldleaf.core.data.dto.farm.FarmerDashboardData
import com.goldleaf.feature.farmermanagement.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class FarmerManagementViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository
) : ViewModel() {
    private var farmerId: String? = null
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(FarmerManagementUiState())
    val uiState: StateFlow<FarmerManagementUiState> = _uiState.asStateFlow()

    fun loadDashboard(farmerId: String) {
        // Cancel previous load if any
        loadJob?.cancel()
        
        this.farmerId = farmerId // 👈 Save it for later refresh

        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Retry logic: Try up to 3 times with exponential backoff
            var lastError: String? = null
            for (attempt in 1..3) {
                when (val result = farmerRepository.getFarmerDashboard(farmerId)) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                dashboardData = result.data,
                                error = null
                            )
                        }
                        return@launch  // ✅ Success, exit
                    }
                    is Result.Error -> {
                        lastError = result.message
                        if (attempt < 3) {
                            // Wait before retrying: 1s, 2s, 4s
                            delay((1000L * attempt * 2))
                        }
                    }
                }
            }
            
            // All retries failed
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = lastError ?: "Failed to load dashboard"
                )
            }
        }
    }

    fun refresh() {
        farmerId?.let { loadDashboard(it) }
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
    }
}

data class FarmerManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dashboardData: FarmerDashboardData? = null
)