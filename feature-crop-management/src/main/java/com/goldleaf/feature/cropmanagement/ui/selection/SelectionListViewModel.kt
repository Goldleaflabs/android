package com.goldleaf.feature.cropmanagement.ui.selection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropMasterEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.ui.activity.CropInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



import java.util.UUID
@HiltViewModel
class SelectionListViewModel @Inject constructor(
    private val cropRepository: CropRepository,
    private val userSession: UserSessionManager, // ✅ Hilt injects it here
    savedStateHandle: androidx.lifecycle.SavedStateHandle  // ✅ Get farmId from nav args
) : ViewModel() {

    private val farmId: String = checkNotNull(savedStateHandle["farmId"])
    
    private val _uiState = MutableStateFlow(SelectionUiState(isLoading = true))
    val uiState: StateFlow<SelectionUiState> = _uiState.asStateFlow()

    init {
        observeDatabase()
        refreshCatalog()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            // Combine two flows:
            // 1. All crops from catalog
            // 2. Crops already on this farm
            cropRepository.getAllCrops().collect { catalogEntities ->
                // Get the farm's current crops (exclude these from the list)
                val farmCrops = cropRepository.getCropsByFarmId(farmId)
                val farmCropNames = farmCrops
                    .map { it.name.trim().lowercase() }
                    .toSet()

                // Filter catalog to show only crops NOT already on the farm.
                // CropEntity.id is an instance id, so matching by id does not work here.
                val availableCrops = catalogEntities.filter { catalogCrop ->
                    catalogCrop.cropName.trim().lowercase() !in farmCropNames
                }

                val mapped = availableCrops.map { entity ->
                    mapToCropInfo(entity)
                }

                _uiState.update { it.copy(
                    catalogCrops = mapped,
                    rawEntities = availableCrops,
                    isLoading = false
                ) }
            }
        }
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            // Only show loading if the list is currently empty
            if (_uiState.value.catalogCrops.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                cropRepository.syncCrops()
            } catch (e: Exception) {
                // If sync fails and we still have no data, stop the spinner so we can show "Retry"
                if (_uiState.value.catalogCrops.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }


    fun addCropToFarm(farmId: String, selectedCrop: CropInfo) {

        Log.d("SelectionViewModel", "🔥 addCropToFarm CALLED - farmId: $farmId, crop: ${selectedCrop.name}")

        viewModelScope.launch {
            Log.d("SelectionViewModel", "🚀 Inside coroutine launch")

            // 1. Disable the specific crop card
            _uiState.update { it.copy(
                savingCropIds = it.savingCropIds + selectedCrop.id
            )}

            Log.d("SelectionViewModel", "✅ Updated savingCropIds: ${_uiState.value.savingCropIds}")

            val farmerId = userSession.currentUserId.value
                ?: userSession.currentUserId.first() // Fallback to fetching first emission
                ?: return@launch

            Log.d("SelectionViewModel", "👤 FarmerId: $farmerId")

            val now = java.time.LocalDateTime.now().toString()
            val cropInstanceId = UUID.randomUUID().toString()

            Log.d("SelectionViewModel", "📡 About to call createCrop...")

            val result = cropRepository.createCrop(
                CropEntity(
                    id = cropInstanceId,
                    farmId = farmId,
                    farmerId = farmerId,
                    name = selectedCrop.name,
                    variety = selectedCrop.variety,
                    createdAt = now,
                    updatedAt = now
                )
            )
            Log.d("SelectionViewModel", "📥 Result received: ${result.isSuccess}")

            // 2. Handle outcome
            if (result.isSuccess) {
                // ✅ Show snackbar and navigate immediately - NO DELAY (local data already saved)
                _uiState.update { it.copy(
                    snackbarMessage = "${selectedCrop.name} added to farm!",
                    savingCropIds = it.savingCropIds - selectedCrop.id,
                    isSaveSuccess = true  // Navigate immediately after local save
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

data class SelectionUiState(
    val isLoading: Boolean = false,
    val catalogCrops: List<CropInfo> = emptyList(),
    val rawEntities: List<CropMasterEntity> = emptyList(),
    val farmerId: String? = null, // ✅ Fixed: Added '?' to allow null
    val savingCropIds: Set<String> = emptySet(), // Tracks which items are currently saving
    val snackbarMessage: String? = null,
    val isSaveSuccess: Boolean = false
)
