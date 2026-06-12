package com.goldleaf.feature.cropmanagement.ui.compliance

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.ComplianceChecklistEntity
import com.goldleaf.core.data.local.dao.ComplianceChecklistDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class ComplianceUiState(
    val items: List<ComplianceChecklistEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "ALL",
    val isLoading: Boolean = true,
    val syncMessage: String? = null,
    val captureItemId: String? = null
)

@HiltViewModel
class ComplianceTrackingViewModel @Inject constructor(
    private val dao: ComplianceChecklistDao,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
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

    fun captureEvidence(item: ComplianceChecklistEntity, photoUri: Uri) {
        viewModelScope.launch {
            val localPath = savePhotoToLocalStorage(photoUri, item.id)
            if (localPath == null) return@launch

            val updated = item.copy(
                evidenceLocalPath = localPath,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateItem(updated)
            uploadPhotoToServer(updated, localPath)
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

    private suspend fun savePhotoToLocalStorage(photoUri: Uri, itemId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "compliance_evidence")
                if (!dir.exists()) dir.mkdirs()
                val dest = File(dir, "${itemId}.jpg")
                context.contentResolver.openInputStream(photoUri)?.use { input ->
                    FileOutputStream(dest).use { output ->
                        input.copyTo(output)
                    }
                }
                dest.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun uploadPhotoToServer(item: ComplianceChecklistEntity, localPath: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(localPath)
                if (!file.exists()) return@withContext

                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("photo", file.name, requestBody)
                val response = apiService.uploadCompliancePhoto(item.id, part)

                if (response.isSuccessful && response.body() != null) {
                    val url = response.body()!!.evidenceUrl
                    val updated = item.copy(evidenceUrl = url)
                    withContext(Dispatchers.Main) {
                        dao.updateItem(updated)
                    }
                }
            } catch (_: Exception) {}
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
