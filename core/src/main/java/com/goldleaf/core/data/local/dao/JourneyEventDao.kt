package com.goldleaf.core.data.local.dao


// core/src/main/java/com/goldleaf/core/data/local/dao/JourneyEventDao.kt


import androidx.room.*
import com.goldleaf.core.data.local.JourneyEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyEventDao {

    @Query("SELECT * FROM journey_events WHERE journeyId = :journeyId ORDER BY timestamp DESC")
    fun getEventsByJourney(journeyId: String): Flow<List<JourneyEventEntity>>

    @Query("SELECT * FROM journey_events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): JourneyEventEntity?

    @Query("SELECT * FROM journey_events WHERE journeyId = :journeyId AND eventType = :eventType")
    fun getEventsByType(journeyId: String, eventType: String): Flow<List<JourneyEventEntity>>

    @Query("SELECT * FROM journey_events WHERE journeyId = :journeyId ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstEvent(journeyId: String): JourneyEventEntity?

    @Query("SELECT * FROM journey_events WHERE journeyId = :journeyId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEvent(journeyId: String): JourneyEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: JourneyEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<JourneyEventEntity>)

    @Update
    suspend fun updateEvent(event: JourneyEventEntity)

    @Delete
    suspend fun deleteEvent(event: JourneyEventEntity)

    @Query("DELETE FROM journey_events WHERE journeyId = :journeyId")
    suspend fun deleteEventsByJourney(journeyId: String)
}