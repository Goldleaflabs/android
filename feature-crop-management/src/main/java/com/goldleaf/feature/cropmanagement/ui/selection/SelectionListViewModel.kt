package com.goldleaf.feature.cropmanagement.ui.selection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropMasterEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.local.PlotEntity
import com.goldleaf.core.data.local.dao.PlotDao
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.ui.activity.CropInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SelectionUiState(
    val isLoading: Boolean = false,
    val catalogCrops: List<CropInfo> = emptyList(),
    val rawEntities: List<CropMasterEntity> = emptyList(),
    val savingCropIds: Set<String> = emptySet(),
    val snackbarMessage: String? = null,
    val isSaveSuccess: Boolean = false,
    // Plot selection
    val plots: List<PlotEntity> = emptyList(),
    val selectedCrop: CropInfo? = null,
    val showPlotDialog: Boolean = false
)

@HiltViewModel
class SelectionListViewModel @Inject constructor(
    private val cropRepository: CropRepository,
    private val plotDao: PlotDao,
    private val userSession: UserSessionManager,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val farmId: String = checkNotNull(savedStateHandle["farmId"])

    private val _uiState = MutableStateFlow(SelectionUiState(isLoading = true))
    val uiState: StateFlow<SelectionUiState> = _uiState.asStateFlow()

    init {
        observeDatabase()
        refreshCatalog()
        loadPlots()
    }

    private fun loadPlots() {
        viewModelScope.launch {
            val plots = plotDao.getPlotsByFarmId(farmId)
            _uiState.update { it.copy(plots = plots) }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            cropRepository.getAllCrops().collect { catalogEntities ->
                val farmCrops = cropRepository.getCropsByFarmId(farmId)
                val activeStatuses = listOf(CropStatus.PLANNED, CropStatus.PLANTED, CropStatus.GROWING)
                val activeFarmCrops = farmCrops.filter { it.status in activeStatuses }
                val farmCropIds = activeFarmCrops.map { it.cropId }.filter { it.isNotEmpty() }.toSet()
                val farmCropNames = activeFarmCrops.map { it.name.trim().lowercase() }.toSet()

                val availableCrops = catalogEntities.filter { catalogCrop ->
                    catalogCrop.cropId !in farmCropIds &&
                    catalogCrop.cropName.trim().lowercase() !in farmCropNames
                }

                val mapped = availableCrops.map { mapToCropInfo(it) }
                _uiState.update { it.copy(catalogCrops = mapped, rawEntities = availableCrops, isLoading = false) }
            }
        }
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            if (_uiState.value.catalogCrops.isEmpty()) _uiState.update { it.copy(isLoading = true) }
            try { cropRepository.syncCrops() }
            catch (e: Exception) {
                if (_uiState.value.catalogCrops.isEmpty()) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Called when user taps a crop card — shows plot selection dialog */
    fun onCropSelected(crop: CropInfo) {
        _uiState.update { it.copy(selectedCrop = crop, showPlotDialog = true) }
    }

    fun dismissPlotDialog() {
        _uiState.update { it.copy(showPlotDialog = false, selectedCrop = null) }
    }

    /** Called after user selects a plot and confirms */
    fun addCropWithPlot(selectedCrop: CropInfo, plotId: String?) {
        Log.d("SelectionViewModel", "addCropToFarm - farmId: $farmId, crop: ${selectedCrop.name}, plot: $plotId")
        _uiState.update { it.copy(showPlotDialog = false) }

        viewModelScope.launch {
            _uiState.update { it.copy(savingCropIds = it.savingCropIds + selectedCrop.id) }

            val farmerId = userSession.currentUserId.value
                ?: userSession.getCurrentUserId()
                ?: return@launch

            val now = java.time.LocalDateTime.now().toString()
            val cropInstanceId = UUID.randomUUID().toString()

            val result = cropRepository.createCrop(
                CropEntity(
                    id = cropInstanceId,
                    farmId = farmId,
                    farmerId = farmerId,
                    name = selectedCrop.name,
                    variety = selectedCrop.variety,
                    plotId = plotId,
                    createdAt = now,
                    updatedAt = now,
                    cropId = selectedCrop.id
                )
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(
                    snackbarMessage = "${selectedCrop.name} added to farm!",
                    savingCropIds = it.savingCropIds - selectedCrop.id,
                    isSaveSuccess = true
                )}
            } else {
                _uiState.update { it.copy(
                    snackbarMessage = "Failed to save. Try again.",
                    savingCropIds = it.savingCropIds - selectedCrop.id
                )}
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun mapToCropInfo(entity: CropMasterEntity): CropInfo {
        return CropInfo(
            id = entity.cropId,
            name = entity.cropName,
            variety = entity.category,
            status = CropStatus.PLANNED
        )
    }
}
