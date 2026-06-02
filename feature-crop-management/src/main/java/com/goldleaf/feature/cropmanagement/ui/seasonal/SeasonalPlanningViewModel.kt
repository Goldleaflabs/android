package com.goldleaf.feature.cropmanagement.ui.seasonal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.SeasonalPlanEntity
import com.goldleaf.core.data.local.dao.SeasonalPlanDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SeasonalPlanUiState(
    val plans: List<SeasonalPlanEntity> = emptyList(),
    val selectedSeason: String = "ALL",
    val isLoading: Boolean = true,
    val syncMessage: String? = null
)

@HiltViewModel
class SeasonalPlanningViewModel @Inject constructor(
    private val planDao: SeasonalPlanDao,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeasonalPlanUiState())
    val uiState: StateFlow<SeasonalPlanUiState> = _uiState.asStateFlow()

    private val _allPlans = MutableStateFlow<List<SeasonalPlanEntity>>(emptyList())

    fun loadPlans(farmId: String) {
        viewModelScope.launch {
            planDao.getPlansByFarm(farmId).collect { plans ->
                _allPlans.value = plans
                applyFilter()
            }
        }
    }

    fun setSeasonFilter(season: String) {
        _uiState.value = _uiState.value.copy(selectedSeason = season)
        applyFilter()
    }

    private fun applyFilter() {
        val season = _uiState.value.selectedSeason
        val plans = if (season == "ALL") {
            _allPlans.value
        } else {
            _allPlans.value.filter { it.season == season }
        }
        _uiState.value = _uiState.value.copy(plans = plans, isLoading = false)
    }

    fun addPlan(
        farmId: String,
        farmerId: String?,
        cropId: String?,
        plotId: String? = null,
        title: String,
        description: String?,
        eventType: String,
        startDate: Long,
        endDate: Long?,
        season: String
    ) {
        viewModelScope.launch {
            val plan = SeasonalPlanEntity(
                id = UUID.randomUUID().toString(),
                farmId = farmId,
                farmerId = farmerId,
                cropId = cropId,
                plotId = plotId,
                title = title,
                description = description,
                eventType = eventType,
                startDate = startDate,
                endDate = endDate,
                season = season
            )
            planDao.insertPlan(plan)
            syncToServer(listOf(plan))
        }
    }

    fun toggleComplete(plan: SeasonalPlanEntity) {
        viewModelScope.launch {
            val updated = plan.copy(
                isCompleted = !plan.isCompleted,
                completedAt = if (!plan.isCompleted) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis()
            )
            planDao.updatePlan(updated)
            syncToServer(listOf(updated))
        }
    }

    fun deletePlan(plan: SeasonalPlanEntity) {
        viewModelScope.launch {
            planDao.deletePlan(plan)
            syncToServer(listOf(plan.copy(isCompleted = true))) // signal deletion via completed status
        }
    }

    private suspend fun syncToServer(plans: List<SeasonalPlanEntity>) {
        try {
            val response = apiService.syncSeasonalPlans(plans)
            if (response.isSuccessful) {
                _uiState.value = _uiState.value.copy(syncMessage = "Synced")
            } else {
                _uiState.value = _uiState.value.copy(
                    syncMessage = "Sync failed: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                syncMessage = "Offline - saved locally"
            )
        }
    }
}
