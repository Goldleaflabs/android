package com.goldleaf.feature.cropmanagement.ui.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.ComplianceChecklistEntity
import com.goldleaf.core.data.local.dao.ComplianceChecklistDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ComplianceUiState(
    val items: List<ComplianceChecklistEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "ALL",
    val isLoading: Boolean = true,
    val syncMessage: String? = null
)

@HiltViewModel
class ComplianceTrackingViewModel @Inject constructor(
    private val dao: ComplianceChecklistDao,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComplianceUiState())
    val uiState: StateFlow<ComplianceUiState> = _uiState.asStateFlow()

    private val _allItems = MutableStateFlow<List<ComplianceChecklistEntity>>(emptyList())

    fun loadItems(farmId: String) {
        viewModelScope.launch {
            dao.getItemsByFarm(farmId).collect { items ->
                _allItems.value = items
                val cats = items.map { it.category }.distinct().sorted()
                _uiState.value = _uiState.value.copy(categories = cats)
                applyFilter()
            }
        }
    }

    fun setCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilter()
    }

    private fun applyFilter() {
        val cat = _uiState.value.selectedCategory
        val items = if (cat == "ALL") _allItems.value
        else _allItems.value.filter { it.category == cat }
        _uiState.value = _uiState.value.copy(items = items, isLoading = false)
    }

    fun addItem(
        farmId: String, farmerId: String?, category: String,
        itemName: String, description: String?, dueDate: Long?
    ) {
        viewModelScope.launch {
            val item = ComplianceChecklistEntity(
                id = UUID.randomUUID().toString(),
                farmId = farmId,
                farmerId = farmerId,
                category = category,
                itemName = itemName,
                description = description,
                status = "PENDING",
                evidenceUrl = null,
                notes = null,
                reviewedBy = null,
                reviewedAt = null,
                dueDate = dueDate
            )
            dao.insertItem(item)
            syncToServer(listOf(item))
        }
    }

    fun updateStatus(item: ComplianceChecklistEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = item.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateItem(updated)
            syncToServer(listOf(updated))
        }
    }

    fun updateEvidence(item: ComplianceChecklistEntity, evidenceUrl: String) {
        viewModelScope.launch {
            val updated = item.copy(
                evidenceUrl = evidenceUrl,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateItem(updated)
            syncToServer(listOf(updated))
        }
    }

    fun updateNotes(item: ComplianceChecklistEntity, notes: String) {
        viewModelScope.launch {
            val updated = item.copy(
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateItem(updated)
            syncToServer(listOf(updated))
        }
    }

    fun deleteItem(item: ComplianceChecklistEntity) {
        viewModelScope.launch {
            dao.deleteItem(item)
        }
    }

    private suspend fun syncToServer(items: List<ComplianceChecklistEntity>) {
        try {
            val response = apiService.syncComplianceChecklist(items)
            if (response.isSuccessful) {
                _uiState.value = _uiState.value.copy(syncMessage = "Synced")
            } else {
                _uiState.value = _uiState.value.copy(syncMessage = "Sync: ${response.code()}")
            }
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(syncMessage = "Offline - saved locally")
        }
    }
}
