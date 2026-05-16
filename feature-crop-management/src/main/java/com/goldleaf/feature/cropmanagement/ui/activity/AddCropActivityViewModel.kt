package com.goldleaf.feature.cropmanagement.ui.activity


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.local.CropActivity
import com.goldleaf.core.data.local.dao.CropDao
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCropActivityViewModel @Inject constructor(
    private val cropDao: CropRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCropActivityUiState())
    val uiState: StateFlow<AddCropActivityUiState> = _uiState.asStateFlow()

    fun loadCropInfo(cropId: String) {
        viewModelScope.launch {
            try {
                val crop = cropDao.getCropById(cropId)
                if (crop != null) {
                    val farm = cropDao.getFarmById(crop.farmId)
                    _uiState.value = _uiState.value.copy(
                        cropInfo = CropInfo(
                            id = crop.id,
                            name = crop.name,
                            variety = crop.variety,
                            status = crop.status,
                            farmName = farm?.name
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load crop info: ${e.message}"
                )
            }
        }
    }

    fun saveActivity(
        cropId: String,
        activityType: ActivityType,
        date: String,
        description: String,
        quantity: Double?,
        unit: String?,
        cost: Double?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val activity = CropActivity(
                    id = UUID.randomUUID().toString(),
                    cropId = cropId,
                    activityType = activityType.name,
                    date = date,
                    description = description,
                    quantity = quantity,
                    unit = unit,
                    cost = cost,
                    notes = notes,
                    createdAt = System.currentTimeMillis()
                )

                cropDao.insertActivity(activity)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to save activity: ${e.message}"
                )
            }
        }
    }
}

data class AddCropActivityUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val cropInfo: CropInfo? = null,
    val error: String? = null
)