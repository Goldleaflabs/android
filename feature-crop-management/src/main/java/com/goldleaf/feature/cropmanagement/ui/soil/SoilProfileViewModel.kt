package com.goldleaf.feature.cropmanagement.ui.soil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.SyncResponseDto
import com.goldleaf.core.data.local.SoilTestEntity
import com.goldleaf.core.data.local.dao.FarmDao
import com.goldleaf.core.data.local.dao.SoilDao
import com.goldleaf.core.data.dto.farm.SoilType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SoilProfileUiState(
    val tests: List<SoilTestEntity> = emptyList(),
    val isLoading: Boolean = true,
    val syncMessage: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class SoilProfileViewModel @Inject constructor(
    private val soilDao: SoilDao,
    private val farmDao: FarmDao,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilProfileUiState())
    val uiState: StateFlow<SoilProfileUiState> = _uiState.asStateFlow()

    fun loadTests(farmId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val tests = soilDao.getSoilTestsByFarmId(farmId)
            _uiState.value = _uiState.value.copy(tests = tests, isLoading = false)
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addTest(
        farmId: String,
        farmerId: String?,
        soilType: String,
        ph: Double,
        nitrogen: Double,
        phosphorus: Double,
        potassium: Double,
        organicMatter: Double?,
        moisture: Double?,
        ec: Double?,
        labName: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val farm = farmDao.getFarmById(farmId)
            val test = SoilTestEntity(
                id = UUID.randomUUID().toString(),
                farmId = farmId,
                farmerId = farmerId,
                testDate = System.currentTimeMillis(),
                soilType = soilType,
                ph = ph,
                nitrogen = nitrogen,
                phosphorus = phosphorus,
                potassium = potassium,
                organicMatter = organicMatter,
                moisture = moisture,
                ec = ec,
                labName = labName,
                notes = notes,
                latitude = farm?.latitude,
                longitude = farm?.longitude,
                createdAt = System.currentTimeMillis(),
                lastSyncTime = System.currentTimeMillis()
            )
            soilDao.insertSoilTest(test)
            syncToServer(listOf(test))
            loadTests(farmId)
            hideAddDialog()
        }
    }

    fun deleteTest(test: SoilTestEntity) {
        viewModelScope.launch {
            soilDao.deleteSoilTest(test)
            loadTests(test.farmId)
        }
    }

    private suspend fun syncToServer(tests: List<SoilTestEntity>) {
        try {
            val response = apiService.syncSoilTests(tests)
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
