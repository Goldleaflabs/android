package com.goldleaf.certification.presentation.batch

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.local.LabTest
import com.goldleaf.core.data.local.BlockchainRecord
import com.goldleaf.certification.domain.repository.BatchRepository
import com.goldleaf.certification.utils.PDFLabelGenerator
import com.goldleaf.certification.utils.QRCodeGenerator
import com.goldleaf.core.auth.UserSession
import com.goldleaf.core.data.local.FarmerEntity
import com.goldleaf.core.data.local.ProductBatchEntity
import com.goldleaf.core.data.local.CropEntity
import com.goldleaf.core.data.local.CropStatus
import com.goldleaf.core.data.pipeline.PipelineRepository
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import com.goldleaf.feature.cropmanagement.ui.selection.OfficerRepository
import com.goldleaf.feature.cropmanagement.ui.selection.SmsWhatsappNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import android.util.Log

data class LabelPrintRequest(
    val batchId: String,
    val quantity: Double,
    val unit: String,
    val farmerName: String,
    val farmerPhone: String,
    val county: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class PrintStatus {
    object Idle : PrintStatus()
    object Printing : PrintStatus()
    data class Success(val message: String) : PrintStatus()
    data class Error(val message: String) : PrintStatus()
}

@HiltViewModel
class BatchViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val cropRepository: CropRepository,
    private val officerRepository: OfficerRepository,
    private val userSession: UserSession,
    private val notifier: SmsWhatsappNotifier,
    private val pipelineRepository: PipelineRepository
) : ViewModel() {

    // -----------------------------
    // PERSISTENT FARMER STATE
    // -----------------------------
    private val _farmerId = MutableStateFlow<String?>(null)
    val farmerId: StateFlow<String?> = _farmerId.asStateFlow()

    private val _farmerData = MutableStateFlow<FarmerEntity?>(null)
    val farmerData: StateFlow<FarmerEntity?> = _farmerData.asStateFlow()

    companion object {
        private const val TAG = "BatchViewModel"
    }

    // -----------------------------
    // UI STATE (All screen data)
    // -----------------------------
    data class UiState(
        val batches: List<ProductBatchEntity> = emptyList(),
        val selectedBatch: ProductBatchEntity? = null,
        val labTests: List<LabTest> = emptyList(),
        val blockchain: BlockchainRecord? = null,
        val qrCode: Bitmap? = null,
        val readyCrops: List<CropEntity> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
        val printStatus: PrintStatus = PrintStatus.Idle,
        val recentRequests: List<LabelPrintRequest> = emptyList(),
        val farmer: FarmerEntity? = null   // ← ADD THIS LINE
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    // -----------------------------
    // INITIAL LOAD
    // -----------------------------
    init {
        initializeFarmerSession()
    }

    private fun initializeFarmerSession() {
        viewModelScope.launch {
            userSession.currentFarmer.collect { farmer ->
                Log.d(TAG, "👤 Farmer session changed: ${farmer?.let { "ID=${it.id}, Name=${it.name}" } ?: "NULL (logged out)"}")

                if (farmer != null) {
                    // Persist farmer ID and data
                    _farmerId.value = farmer.id
                    _farmerData.value = farmer
                    update { it.copy(farmer = farmer) }
                    Log.d(TAG, "✅ Farmer ID persisted: ${farmer.id}")
                    Log.d(TAG, "📊 Loading farmer data...")

                    // Load all farmer-related data once
                    loadFarmerData()
                } else {
                    // Clear on logout
                    Log.w(TAG, "🚪 Logging out - clearing farmer data")
                    _farmerId.value = null
                    _farmerData.value = null
                    clearAllData()
                }
            }
        }
    }

    private suspend fun loadFarmerData() {
        val id = _farmerId.value ?: return

        Log.d(TAG, "📦 Loading data for farmer: $id")

        // Refresh pipeline definition from server
        pipelineRepository.refresh()

        // Sync batches from server first (populates local database)
        batchRepository.syncBatches(id).onFailure { e ->
            Log.w(TAG, "⚠️ Failed to sync batches from server: ${e.message}")
        }

        // Load all data that depends on farmer ID from local database
        loadBatchesForFarmer(id)
        loadReadyCropsForFarmer(id)

        Log.d(TAG, "✅ Farmer data loaded successfully")
    }

    private fun clearAllData() {
        Log.d(TAG, "🗑️ Clearing all UI data")
        update {
            UiState() // Reset to initial state
        }
    }

    // -----------------------------
    // BATCH LIST
    // -----------------------------
    private suspend fun loadBatchesForFarmer(farmerId: String) {
        Log.d(TAG, "🔍 Loading batches for farmer: $farmerId")
        val all = batchRepository.getAllBatches().first()
        val filtered = all.filter { b -> b.farmerId == farmerId }
        Log.d(TAG, "📦 Found ${filtered.size} batches for farmer $farmerId")
        update { it.copy(batches = filtered) }
    }

    fun loadBatches() = launchSafe {
        val id = requireFarmerId()
        loadBatchesForFarmer(id)
    }

    fun refreshBatches() = loadBatches() // Alias for UI refresh actions

    fun loadBatchDetails(batchId: String) = launchSafe {
        val batch = batchRepository.getBatchById(batchId).first()
        if (batch != null) {
            val qr = QRCodeGenerator.generateQRCode(
                "https://verify.goldleaflabs.co.ke/${batch.batchNumber}", 512
            )
            update {
                it.copy(
                    selectedBatch = batch,
                    qrCode = qr
                )
            }
            loadLabTests(batchId)
            loadBlockchainRecord(batchId)
        }
    }

    private suspend fun loadLabTests(batchId: String) {
        batchRepository.getLabTests(batchId).onSuccess { tests ->
            update { it.copy(labTests = tests) }
        }
    }

    private suspend fun loadBlockchainRecord(batchId: String) {
        batchRepository.getBlockchainRecord(batchId).onSuccess { record ->
            update { it.copy(blockchain = record) }
        }
    }

    // -----------------------------
    // CROPS
    // -----------------------------
    private suspend fun loadReadyCropsForFarmer(farmerId: String) {
        Log.d(TAG, "🌱 Loading crops for farmer: $farmerId")
        val harvestStageId = pipelineRepository.getHarvestStageId()
        val allCrops = if (harvestStageId != null) {
            cropRepository.getCropsByStatusAndFarmer(
                listOf(CropStatus.GROWING, CropStatus.PLANNED, CropStatus.HARVESTED),
                farmerId
            )
        } else {
            cropRepository.getCropsByStatusAndFarmer(
                listOf(CropStatus.HARVESTED),
                farmerId
            )
        }

        // Exclude crops that already have a batch
        val batchedCropIds = _ui.value.batches.map { it.cropId }.filter { it.isNotEmpty() }.toSet()
        val pipelineFiltered = if (harvestStageId != null) {
            allCrops.filter {
                it.pipelineStageId == harvestStageId ||
                (it.pipelineStageId == null && it.status == CropStatus.HARVESTED)
            }
        } else {
            allCrops
        }
        val crops = pipelineFiltered.filter { it.id !in batchedCropIds }

        Log.d(TAG, "🌾 Found ${crops.size} ready crops for farmer $farmerId (harvestStageId=$harvestStageId, ${batchedCropIds.size} already batched)")
        update { it.copy(readyCrops = crops) }
    }

    fun loadReadyCrops() = launchSafe {
        val id = requireFarmerId()
        loadReadyCropsForFarmer(id)
    }

    fun refreshCrops() = loadReadyCrops() // Alias for UI refresh actions

    // -----------------------------
    // CREATE BATCH
    // -----------------------------
    fun createHarvestBatch(
        crop: CropEntity,
        bags: Int,
        farmName: String,
        onSuccess: () -> Unit
    ) = launchSafe {
        val id = requireFarmerId()
        val batchId = buildBatchId(farmName)
        val qty = bags.toDouble()
        val harvestDate = java.time.LocalDate.now().toString()

        val processResult = BatchProcessor.createBatchAndRecordHarvest(
            batchRepository = batchRepository,
            cropRepository = cropRepository,
            crop = crop,
            bags = bags,
            qtyKg = qty,
            batchId = batchId,
            farmerId = id,
            farmerName = _farmerData.value?.name ?: "",
            harvestDateIso = harvestDate
        )

        // If batch creation succeeded, handle UI updates and surfacing sync errors
        processResult.batchResult.onSuccess {
            // If crop server-sync failed, show an error in UI
            processResult.cropUpdateResult?.onFailure { err ->
                update { state -> state.copy(error = "Failed to sync crop to server: ${err.message}" ) }
            }

            loadBatchesForFarmer(id)
            loadReadyCropsForFarmer(id)
            onSuccess()
        }
    }

    private fun buildBatchId(farmName: String): String {
        val code = farmName.replace(" ", "").uppercase().take(8)
        val safe = if (code.length < 3) "FARM" else code
        val date = SimpleDateFormat("yyMM").format(Date())
        return "GL-HVST-$safe-$date-${(1000..9999).random()}"
    }

    private suspend fun createBatchForFarmer(
        farmerId: String,
        batchNumber: String,
        productType: String,
        quantity: Double,
        unit: String,
        harvestDate: String
    ): Result<com.goldleaf.core.data.local.ProductBatchEntity> {
        val farmer = requireFarmer()

        val result = batchRepository.createBatch(
            batchNumber,
            productType,
            quantity,
            unit,
            harvestDate,
            farmerId,
            farmer.name
        )

        result.onSuccess {
            loadBatchesForFarmer(farmerId)
        }

        return result
    }

    fun createBatch(
        batchNumber: String,
        productType: String,
        quantity: Double,
        unit: String,
        harvestDate: String
    ) = launchSafe {
        val id = requireFarmerId()
        createBatchForFarmer(id, batchNumber, productType, quantity, unit, harvestDate)
    }

    fun syncBatches() = launchSafe {
        val id = requireFarmerId()
        batchRepository.syncBatches(id)
        loadBatchesForFarmer(id)
    }

    // -----------------------------
    // PRINTING
    // -----------------------------
    fun printLabelsNow(context: Context, batchId: String, qty: Double, unit: String) {
        viewModelScope.launch {
            update { it.copy(printStatus = PrintStatus.Printing) }
            try {
                val batch = _ui.value.batches.firstOrNull { it.batchNumber == batchId }
                    ?: _ui.value.selectedBatch
                    ?: throw IllegalStateException("Batch data not found for printing")

                val generatedFile = withContext(Dispatchers.IO) {
                    val runtime = Runtime.getRuntime()
                    val freeMemoryMb = (runtime.freeMemory() / (1024 * 1024)).toInt()
                    if (freeMemoryMb < 32) {
                        throw IllegalStateException("Insufficient memory to generate label. Close other apps and retry.")
                    }

                    val qrBitmap = _ui.value.qrCode
                        ?: QRCodeGenerator.generateQRCode(
                            "https://verify.goldleaflabs.co.ke/${batch.batchNumber}",
                            512
                        )

                    PDFLabelGenerator.generatePrintableLabel(
                        context = context,
                        batch = batch,
                        qrCode = qrBitmap
                    )
                }

                update {
                    it.copy(printStatus = PrintStatus.Success("Label saved: ${generatedFile.name}"))
                }
            } catch (e: Exception) {
                update { it.copy(printStatus = PrintStatus.Error(e.message ?: "Error")) }
            } catch (e: OutOfMemoryError) {
                update { it.copy(printStatus = PrintStatus.Error("Not enough memory to generate label")) }
            }
        }
    }

    // -----------------------------
    // REQUEST LABELS FROM OFFICER
    // -----------------------------
    fun requestLabelsFromOfficer(
        batchId: String,
        qty: Double,
        unit: String,
        county: String
    ) = launchSafe {
        Log.d(TAG, "📤 requestLabelsFromOfficer() CALLED → batchId=$batchId, qty=$qty, unit=$unit, county='$county'")
        
        val farmer = requireFarmer()  // Safe, throws if not logged in
        Log.d(TAG, "✅ Farmer loaded: Name=${farmer.name}, Phone=${farmer.phone}")

        Log.d(TAG, "🔍 Looking up officer phone for county: '$county'")
        val officerPhone = officerRepository.getOfficerPhoneByCounty(county)
        Log.d(TAG, "📞 Officer phone lookup result: $officerPhone (using fallback: ${officerPhone == null})")
        
        val finalPhone = officerPhone ?: "254725999521"
        Log.d(TAG, "📱 Final phone to use: $finalPhone")

        val msg = """
        🏷️ NEW LABEL REQUEST
        
        Farmer: ${farmer.name}
        Phone: ${farmer.phone}
        County: $county
        
        Batch ID: $batchId
        Quantity: $qty $unit
        
        🖨️ Print: https://officer.goldleaflabs.co.ke/print?batch=$batchId
        ✅ Verify: https://verify.goldleaflabs.co.ke/$batchId
    """.trimIndent()

        Log.d(TAG, "📨 Sending WhatsApp request to $finalPhone...")
        notifier.sendRequest(finalPhone, msg)
        Log.d(TAG, "✅ WhatsApp intent launched successfully")

        val req = LabelPrintRequest(
            batchId = batchId,
            quantity = qty,
            unit = unit,
            farmerName = farmer.name,
            farmerPhone = farmer.phone,
            county = county
        )
        update { it.copy(recentRequests = listOf(req) + it.recentRequests.take(9)) }
        Log.d(TAG, "✅ Request recorded in UI state")
    }
    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun requireFarmerId(): String {
        val id = _farmerId.value
        Log.d(TAG, "🔐 Farmer ID requested: ${id ?: "NULL"}")
        return id ?: throw IllegalStateException("No farmer ID available. User may not be logged in.")
    }

    private fun requireFarmer(): FarmerEntity {
        val farmer = _farmerData.value
        Log.d(TAG, "🔐 Farmer data requested: ${farmer?.let { "ID=${it.id}, Name=${it.name}" } ?: "NULL"}")
        return farmer ?: throw IllegalStateException("No farmer data available. User may not be logged in.")
    }

    private fun update(reducer: (UiState) -> UiState) {
        _ui.value = reducer(_ui.value)
    }

    private fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                update { it.copy(loading = true, error = null) }
                block()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in operation: ${e.message}", e)
                update { it.copy(error = e.message) }
            } finally {
                update { it.copy(loading = false) }
            }
        }
    }

    fun resetPrintStatus() = update { it.copy(printStatus = PrintStatus.Idle) }
    fun clearError() = update { it.copy(error = null) }

    // -----------------------------
    // LOGOUT
    // -----------------------------
    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "🚪 Logout called - clearing session")
            userSession.clearSession() // This should trigger the collector to clear state
        }
    }
}
