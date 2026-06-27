package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.Officer

@Dao
interface OfficerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficers(officers: List<Officer>)

    @Query("SELECT * FROM officers")
    suspend fun getAllOfficers(): List<Officer>

    @Query("SELECT * FROM officers WHERE id = :id")
    suspend fun getOfficerById(id: Int): Officer?
}
