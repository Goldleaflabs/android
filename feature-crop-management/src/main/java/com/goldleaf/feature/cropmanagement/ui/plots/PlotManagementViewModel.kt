package com.goldleaf.feature.cropmanagement.ui.plots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.PlotEntity
import com.goldleaf.core.data.local.dao.PlotDao
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.core.data.local.dao.CropMasterDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PlotUiState(
    val plots: List<PlotEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class PlotManagementViewModel @Inject constructor(
    private val plotDao: PlotDao,
    private val cropDao: CropDao,
    private val cropMasterDao: CropMasterDao,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlotUiState())
    val uiState: StateFlow<PlotUiState> = _uiState.asStateFlow()

    fun loadPlots(farmId: String) {
        viewModelScope.launch {
            val plots = plotDao.getPlotsByFarmId(farmId)
            _uiState.value = _uiState.value.copy(plots = plots)
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addPlot(farmId: String, name: String, size: Double, sizeUnit: String, soilType: String?, notes: String?, color: String) {
        viewModelScope.launch {
            val plot = PlotEntity(
                id = UUID.randomUUID().toString(),
                farmId = farmId,
                name = name,
                size = size,
                sizeUnit = sizeUnit,
                soilType = soilType,
                notes = notes,
                color = color,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            plotDao.insertPlot(plot)
            syncToServer(listOf(plot))
            loadPlots(farmId)
            hideAddDialog()
        }
    }

    fun deletePlot(plot: PlotEntity) {
        viewModelScope.launch {
            plotDao.deletePlot(plot)
            loadPlots(plot.farmId)
        }
    }

    private suspend fun syncToServer(plots: List<PlotEntity>) {
        try {
            val response = apiService.syncPlots(plots)
            if (response.isSuccessful) {
                _uiState.value = _uiState.value.copy(syncMessage = "Synced")
            } else {
                _uiState.value = _uiState.value.copy(syncMessage = "Sync failed: ${response.code()}")
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(syncMessage = "Offline - saved locally")
        }
    }
}
