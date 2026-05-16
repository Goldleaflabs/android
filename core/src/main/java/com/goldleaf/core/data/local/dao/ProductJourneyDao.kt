package com.goldleaf.core.data.local.dao


// core/src/main/java/com/goldleaf/core/data/local/dao/ProductJourneyDao.kt


import androidx.room.*
import com.goldleaf.core.data.local.ProductJourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductJourneyDao {

    @Query("SELECT * FROM product_journeys WHERE batchId = :batchId")
    suspend fun getJourneyByBatch(batchId: String): ProductJourneyEntity?

    @Query("SELECT * FROM product_journeys WHERE id = :journeyId")
    suspend fun getJourneyById(journeyId: String): ProductJourneyEntity?

    @Query("SELECT * FROM product_journeys WHERE Status = :status")
    fun getJourneysByStatus(status: String): Flow<List<ProductJourneyEntity>>

    @Query("SELECT * FROM product_journeys ORDER BY startDate DESC")
    fun getAllJourneys(): Flow<List<ProductJourneyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: ProductJourneyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneys(journeys: List<ProductJourneyEntity>)

    @Update
    suspend fun updateJourney(journey: ProductJourneyEntity)

    @Delete
    suspend fun deleteJourney(journey: ProductJourneyEntity)
}
