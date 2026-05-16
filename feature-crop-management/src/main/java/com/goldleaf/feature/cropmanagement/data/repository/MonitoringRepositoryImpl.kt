package com.goldleaf.feature.cropmanagement.data.repository

import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.auth.CropActivityRequest
import com.goldleaf.core.data.local.CropMonitoringRecord
import com.goldleaf.core.data.local.CropMonitoringRecordEntity
import com.goldleaf.core.data.local.dao.CropMonitoringDao
import com.goldleaf.feature.cropmanagement.domain.repository.MonitoringRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MonitoringRepositoryImpl @Inject constructor(
    private val cropMonitoringDao: CropMonitoringDao,
    private val apiService: ApiService,
    private val userSessionManager: UserSessionManager
) : MonitoringRepository {

    override fun getAllMonitoringRecords(): Flow<List<CropMonitoringRecord>> {
        return cropMonitoringDao.getAllRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMonitoringRecordById(id: String): CropMonitoringRecord? {
        return cropMonitoringDao.getAllRecords().first().find { it.id == id }?.toDomain()
    }

    override suspend fun getMonitoringRecordsByCropId(cropId: String): Result<List<CropMonitoringRecord>> {
        return try {
            val records = cropMonitoringDao.getRecordsByCropId(cropId).first()
            Result.success(records.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun insertMonitoringRecord(record: CropMonitoringRecord): Result<CropMonitoringRecord> {
        return try {
            android.util.Log.d("🌾 MONITORING", "💾 Repository.insertMonitoringRecord() - Preparing to save/sync record")
            
            // 1) Push to server (best-effort)
            val request = CropActivityRequest(
                type = "MONITORING",
                description = buildString {
                    append(record.notes ?: "Monitoring update")
                    record.healthStatus.let { append(" | Health: ").append(it) }
                    record.moistureLevel?.let { append(" | Moisture: ").append(it) }
                    record.pestObservations?.let { append(" | Pests: ").append(it) }
                    record.diseaseObservations?.let { append(" | Disease: ").append(it) }
                },
                date = record.recordDate,
                cost = null
            )

            val remoteResult = runCatching {
                android.util.Log.d("🌾 MONITORING", "🌐 Attempting server sync for cropId=${record.cropId}")
                apiService.addCropActivity(record.cropId, request)
            }.getOrNull()

            val remoteId = if (remoteResult != null && remoteResult.isSuccessful) {
                android.util.Log.d("🌾 MONITORING", "✅ Server sync SUCCESS: remoteId=${remoteResult.body()?.id}")
                remoteResult.body()?.id
            } else {
                android.util.Log.w("🌾 MONITORING", "⚠️ Server sync failed or no response, proceeding with local save")
                null
            }

            val enriched = record.copy(
                id = if (record.id.isNotBlank()) record.id else remoteId ?: generateLocalId(),
                recordedBy = record.recordedBy ?: userSessionManager.getCurrentUserIdSync()
            )

            android.util.Log.d("🌾 MONITORING", "💿 Writing to LOCAL DATABASE: id=${enriched.id}, cropId=${enriched.cropId}")
            // 2) Always persist locally
            cropMonitoringDao.insertRecord(enriched.toEntity())
            android.util.Log.d("🌾 MONITORING", "✅ LOCAL DATABASE insertion complete for id=${enriched.id}")
            
            Result.success(enriched)
        } catch (e: Exception) {
            android.util.Log.e("🌾 MONITORING", "❌ insertMonitoringRecord FAILED", e)
            Result.failure(e)
        }
    }

    override suspend fun updateMonitoringRecord(record: CropMonitoringRecord): Result<CropMonitoringRecord> {
        return try {
            cropMonitoringDao.updateRecord(record.toEntity())
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMonitoringRecord(id: String): Result<Unit> {
        return try {
            val record = getMonitoringRecordById(id)
            if (record != null) {
                cropMonitoringDao.deleteRecord(record.toEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Record not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mapping functions
    private fun CropMonitoringRecordEntity.toDomain(): CropMonitoringRecord {
        val gson = Gson()
        val photosList = if (this.photos.isBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(this.photos, type)
            } catch (e: Exception) {
                emptyList()
            }
        }

        return CropMonitoringRecord(
            id = this.id,
            cropId = this.cropId,
            recordDate = this.recordDate,
            healthStatus = this.healthStatus,
            moistureLevel = this.moistureLevel,
            pestObservations = this.pestObservations,
            diseaseObservations = this.diseaseObservations,
            weatherConditions = this.weatherConditions,
            photos = photosList, // String → List<String>
            notes = this.notes,
            recordedBy = this.recordedBy
        )
    }

    private fun CropMonitoringRecord.toEntity(): CropMonitoringRecordEntity {
        val gson = Gson()
        return CropMonitoringRecordEntity(
            id = this.id,
            cropId = this.cropId,
            recordDate = this.recordDate,
            healthStatus = this.healthStatus,
            moistureLevel = this.moistureLevel,
            pestObservations = this.pestObservations,
            diseaseObservations = this.diseaseObservations,
            weatherConditions = this.weatherConditions,
            photos = gson.toJson(this.photos),
            notes = this.notes,
            recordedBy = this.recordedBy
        )
    }

    private fun generateLocalId(): String =
        "monitor_${System.currentTimeMillis()}_${(1000..9999).random()}"
}
