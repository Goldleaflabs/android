package com.goldleaf.certification.presentation.quality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.certification.data.remote.CertificationApiService
import com.goldleaf.core.auth.UserSession
import com.goldleaf.core.data.local.AppDatabase
import com.goldleaf.core.data.local.LabTestEntity
import com.goldleaf.core.data.local.Teststatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class TestParameter(
    val name: String,
    val value: String,
    val unit: String,
    val standard: String,
    val status: String // PASS, FAIL, WARNING
)

data class LabTestWithBatch(
    val testId: String,
    val testType: String,
    val labName: String,
    val testDate: String,
    val status: Teststatus,
    val isPassed: Boolean,
    val batchNumber: String,
    val productType: String,
    val parameters: List<TestParameter> = emptyList(),
    val notes: String? = null
)

@HiltViewModel
class QualityViewModel @Inject constructor(
    private val database: AppDatabase,
    private val apiService: CertificationApiService,
    private val userSession: UserSession
) : ViewModel() {

    data class UiState(
        val items: List<LabTestWithBatch> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val labTestDao = database.labTestDao()
    private val batchDao = database.productBatchDao()

    init {
        viewModelScope.launch {
            userSession.currentFarmer.collect { farmer ->
                if (farmer != null) {
                    loadData(farmer.id)
                }
            }
        }
    }

    private fun loadData(farmerId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }

            withContext(Dispatchers.IO) {
                try {
                    val batches = batchDao.getAllBatches().first()
                    batches.filter { it.farmerId == farmerId }.forEach { batch ->
                        try {
                            val token = userSession.getAuthToken() ?: return@forEach
                            val response = apiService.getLabTests("Bearer $token", batch.id)
                            if (response.isSuccessful && response.body() != null) {
                                val entities = response.body()!!.map { dto ->
                                    LabTestEntity(
                                        id = dto.id,
                                        batchId = dto.batchId,
                                        testType = dto.testType,
                                        labName = dto.labName,
                                        testDate = dto.testDate.toLongOrNull() ?: System.currentTimeMillis(),
                                        results = dto.results ?: "",
                                        status = when (dto.status.uppercase()) {
                                            "PASSED" -> Teststatus.PASSED
                                            "FAILED" -> Teststatus.FAILED
                                            else -> Teststatus.WARNING
                                        },
                                        reportUrl = dto.resultUrl,
                                        certifiedBy = null,
                                        farmerId = farmerId,
                                        farmId = null
                                    )
                                }
                                labTestDao.insertTests(entities)
                            }
                        } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }

            combine(
                labTestDao.getAllTests(),
                batchDao.getAllBatches()
            ) { tests, batches ->
                val batchMap = batches.associateBy { it.id }
                tests.map { test ->
                    val batch = batchMap[test.batchId]
                    LabTestWithBatch(
                        testId = test.id,
                        testType = test.testType,
                        labName = test.labName,
                        testDate = test.testDate.toString(),
                        status = test.status,
                        isPassed = test.status == Teststatus.PASSED,
                        batchNumber = batch?.batchNumber ?: test.batchId.take(8),
                        productType = batch?.productType ?: "--",
                        parameters = parseParameters(test.results),
                        notes = null
                    )
                }
            }.collect { items ->
                _ui.update { it.copy(items = items, loading = false) }
            }
        }
    }

    private fun parseParameters(resultsJson: String): List<TestParameter> {
        if (resultsJson.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(resultsJson)
            val list = mutableListOf<TestParameter>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(TestParameter(
                    name = obj.optString("name").takeIf { it.isNotBlank() }
                        ?: obj.optString("parameter").takeIf { it.isNotBlank() }
                        ?: obj.optString("test").takeIf { it.isNotBlank() } ?: "--",
                    value = obj.optString("value").takeIf { it.isNotBlank() }
                        ?: obj.optString("result").takeIf { it.isNotBlank() } ?: "--",
                    unit = obj.optString("unit"),
                    standard = obj.optString("standard").takeIf { it.isNotBlank() }
                        ?: obj.optString("range").takeIf { it.isNotBlank() }
                        ?: obj.optString("limit").takeIf { it.isNotBlank() } ?: "",
                    status = (obj.optString("status").takeIf { it.isNotBlank() } ?: "PASS").uppercase()
                ))
            }
            list
        } catch (_: Exception) {
            // Try flat object: {"Moisture": "12.5", "pH": "6.8"}
            try {
                val obj = JSONObject(resultsJson)
                val keys = obj.keys()
                val list = mutableListOf<TestParameter>()
                while (keys.hasNext()) {
                    val key = keys.next()
                    list.add(TestParameter(
                        name = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                        value = obj.optString(key),
                        unit = "", standard = "", status = "PASS"
                    ))
                }
                list
            } catch (_2: Exception) {
                emptyList()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val farmerId = userSession.currentFarmer.first()?.id ?: return@launch
            loadData(farmerId)
        }
    }
}
