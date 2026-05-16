package com.goldleaf.feature.cropmanagement.ui.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.local.*
import com.goldleaf.feature.cropmanagement.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CropMonitoringViewModel @Inject constructor(
    private val getCropsUseCase: GetCropsUseCase,
    private val updateCropUseCase: UpdateCropUseCase,
    private val getYieldAnalyticsUseCase: GetYieldAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropMonitoringUiState())
    val uiState: StateFlow<CropMonitoringUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(CropFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _sortBy = MutableStateFlow(SortBy.PLANTING_DATE)

    init {
        observeCrops()
        loadYieldAnalytics()
    }

    // ------------------------------------------------------------------
    // CORE OBSERVATION PIPELINE (single source of truth)
    // ------------------------------------------------------------------
    private fun observeCrops() {
        viewModelScope.launch {
            combine(
                getCropsUseCase(),
                _filter,
                _searchQuery,
                _sortBy
            ) { crops, filter, query, sortBy ->
                applyFilterSort(crops, filter, query, sortBy)
            }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load crops"
                    )
                }
                .collect { crops ->
                    _uiState.value = _uiState.value.copy(
                        crops = crops,
                        analytics = calculateAnalytics(crops),
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    // ------------------------------------------------------------------
    // FILTER / SEARCH / SORT
    // ------------------------------------------------------------------
    private fun applyFilterSort(
        crops: List<CropEntity>,
        filter: CropFilter,
        query: String,
        sortBy: SortBy
    ): List<CropEntity> {

        var result = crops

        result = when (filter) {
            CropFilter.ALL -> result
            CropFilter.ACTIVE ->
                result.filter { it.status in listOf(CropStatus.PLANTED, CropStatus.GROWING) }
            CropFilter.COMPLETED ->
                result.filter { it.status == CropStatus.COMPLETED }
            CropFilter.HARVESTED ->
                result.filter { it.status == CropStatus.HARVESTED }
            CropFilter.NEEDS_ATTENTION ->
                result.filter { needsAttention(it) }
            CropFilter.READY ->
                result.filter { it.harvestDate != null && it.status == CropStatus.GROWING }
        }

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, true) ||
                        it.variety?.contains(query, true) == true ||
                        it.location?.contains(query, true) == true
            }
        }

        result = when (sortBy) {
            SortBy.NAME -> result.sortedBy { it.name }
            SortBy.PLANTING_DATE -> result.sortedByDescending { it.plantingDate }
            SortBy.STATUS -> result.sortedBy { it.status?.ordinal }
            SortBy.AREA -> result.sortedByDescending { it.area ?: 0.0 }
            SortBy.EXPECTED_YIELD -> result.sortedByDescending { it.expectedYield ?: 0.0 }
        }

        return result
    }

    private fun needsAttention(crop: CropEntity): Boolean {
        if (crop.status == CropStatus.FAILED) return true

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return crop.harvestDate != null &&
                crop.harvestDate!! < today &&
                (crop.status != CropStatus.COMPLETED)
    }

    // ------------------------------------------------------------------
    // ANALYTICS (NO UI RECOMPUTATION)
    // ------------------------------------------------------------------
    private fun calculateAnalytics(crops: List<CropEntity>): CropMonitoringAnalytics {
        val active = crops.count { it.status in listOf(CropStatus.PLANTED, CropStatus.GROWING) }
        val totalArea = crops.sumOf { it.area ?: 0.0 }

        val completed = crops.filter {
            it.status == CropStatus.COMPLETED &&
                    it.actualYield != null &&
                    it.expectedYield != null &&
                    it.expectedYield!! > 0
        }

        val yieldEfficiency =
            if (completed.isEmpty()) 0.0
            else completed.map {
                (it.actualYield!! / it.expectedYield!!) * 100
            }.average()

        return CropMonitoringAnalytics(
            totalCrops = crops.size,
            activeCrops = active,
            totalArea = totalArea,
            averageYieldEfficiency = yieldEfficiency
        )
    }

    // ------------------------------------------------------------------
    // COMMANDS (TASKS / MONITORING / STATUS)
    // ------------------------------------------------------------------
   fun updateCropStatus(cropId: String, status: CropStatus) {
        viewModelScope.launch {
            val crop = _uiState.value.crops.find { it.id == cropId } ?: return@launch
           try {
                val updated = crop.copy(
                    status = status,
                    updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .format(Date())
                )

                updateCropUseCase(updated)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            message = "Crop updated"
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

    // ------------------------------------------------------------------
    // UI EVENTS
    // ------------------------------------------------------------------
    fun updateFilter(filter: CropFilter) {
        _filter.value = filter
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSort(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun refresh() {
        loadYieldAnalytics()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // ------------------------------------------------------------------
    // YIELD ANALYTICS
    // ------------------------------------------------------------------
    private fun loadYieldAnalytics() {
        viewModelScope.launch {
            getYieldAnalyticsUseCase()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(yieldAnalytics = it)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
        }
    }
}


// UI State


data class CropMonitoringUiState(
    val crops: List<CropEntity> = emptyList(),
    val analytics: CropMonitoringAnalytics? = null,
    val yieldAnalytics: YieldAnalytics? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

data class CropMonitoringAnalytics(
    val totalCrops: Int,
    val activeCrops: Int,
    val totalArea: Double,
    val averageYieldEfficiency: Double
)


// Filter and Sort Options
enum class CropFilter(val displayName: String) {
    ALL("All Crops"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    HARVESTED("Harvested"),
    NEEDS_ATTENTION("Needs Attention"),
    READY("Ready To Collect")
}

enum class SortBy(val displayName: String) {
    NAME("Name"),
    PLANTING_DATE("Planting Date"),
    STATUS("Status"),
    AREA("Area"),
    EXPECTED_YIELD("Expected Yield")
}

// Monitoring Events
sealed class CropMonitoringEvent {
    object RefreshData : CropMonitoringEvent()
    data class UpdateFilter(val filter: CropFilter) : CropMonitoringEvent()
    data class UpdateSearch(val query: String) : CropMonitoringEvent()
    data class UpdateSort(val sortBy: SortBy) : CropMonitoringEvent()
    data class UpdateCropStatus(val cropId: String, val status: CropStatus) : CropMonitoringEvent()
    data class AddMonitoringRecord(val cropId: String, val record: CropMonitoringRecord) : CropMonitoringEvent()
    data class MarkTaskComplete(val cropId: String, val taskId: String) : CropMonitoringEvent()
    object ClearError : CropMonitoringEvent()
    object ClearMessage : CropMonitoringEvent()
}

// Extension functions for additional functionality
fun CropEntity.getDaysUntilHarvest(): Int? {
    harvestDate?.let { harvest ->
        try {
            val harvestDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(harvest)
            val currentDate = Date()
            harvestDate?.let { date ->
                return ((date.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
            }
        } catch (e: Exception) {
            return null
        }
    }
    return null
}

fun CropEntity.getDaysSincePlanting(): Int {
    try {
        val plantingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(plantingDate)
        val currentDate = Date()
        plantingDate?.let { date ->
            return ((currentDate.time - date.time) / (1000 * 60 * 60 * 24)).toInt()
        }
    } catch (e: Exception) {
        return 0
    }
    return 0
}

fun CropEntity.getYieldEfficiency(): Double? {
    return if (actualYield != null && expectedYield != null && expectedYield!! > 0) {
        (actualYield!! / expectedYield!!) * 100
    } else null
}

fun CropEntity.getStatusColor(): androidx.compose.ui.graphics.Color {
    return when (status) {
        CropStatus.PLANNED -> androidx.compose.ui.graphics.Color.Gray
        CropStatus.PLANTED -> androidx.compose.ui.graphics.Color.Blue
        CropStatus.GROWING -> androidx.compose.ui.graphics.Color.Green
        CropStatus.HARVESTED -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        CropStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Dark Green
        CropStatus.FAILED -> androidx.compose.ui.graphics.Color.Red // Add this
        null -> androidx.compose.ui.graphics.Color.Gray

    }
}
