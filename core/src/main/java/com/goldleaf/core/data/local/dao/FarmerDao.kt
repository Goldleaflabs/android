package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.FarmerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("UPDATE farmers SET preferences = :preferencesJson WHERE id = :farmerId")
    suspend fun updateFarmerPreferences(farmerId: String, preferencesJson: String)

    @Query("SELECT * FROM farmers LIMIT 1")
    suspend fun getCurrentFarmerSync(): FarmerEntity?

    @Query("SELECT lastSyncTime FROM farmers LIMIT 1")
    suspend fun getLastSyncTime(): Long?

    @Query("SELECT * FROM farmers WHERE id = :farmerId")
    fun getFarmerById(farmerId: String): Flow<FarmerEntity>

    @Query("SELECT * FROM farmers WHERE id = :farmerId")
    suspend fun getFarmerByIdSingle(farmerId: String): FarmerEntity?

    @Query("SELECT * FROM farmers WHERE phone = :phone LIMIT 1")
    suspend fun getFarmerByPhone(phone: String): FarmerEntity?

    @Query("SELECT * FROM farmers LIMIT 1")
    fun getCurrentFarmer(): Flow<FarmerEntity?>

    @Query("SELECT * FROM farmers LIMIT 1")
    suspend fun getCurrentFarmerSnapshot(): FarmerEntity?

    @Query("SELECT * FROM farmers")
    fun getAllFarmers(): Flow<List<FarmerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity)

    @Update
    suspend fun updateFarmer(farmer: FarmerEntity)

    @Delete
    suspend fun deleteFarmer(farmer: FarmerEntity)

    @Query("DELETE FROM farmers")
    suspend fun deleteAllFarmers()

    @Query("DELETE FROM farms")
    suspend fun deleteAllFarms()

    @Query("DELETE FROM certifications")
    suspend fun deleteAllCertifications()

    @Transaction
    suspend fun clearAllUserData() {
        deleteAllFarmers()
        deleteAllFarms()
        deleteAllCertifications()
    }
}
