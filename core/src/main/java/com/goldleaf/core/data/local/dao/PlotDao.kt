package com.goldleaf.core.data.local.dao

import androidx.room.*
import com.goldleaf.core.data.local.PlotEntity

@Dao
interface PlotDao {
    @Query("SELECT * FROM farm_plots WHERE farmId = :farmId ORDER BY name ASC")
    suspend fun getPlotsByFarmId(farmId: String): List<PlotEntity>

    @Query("SELECT * FROM farm_plots WHERE id = :plotId")
    suspend fun getPlotById(plotId: String): PlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: PlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<PlotEntity>)

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Delete
    suspend fun deletePlot(plot: PlotEntity)
}
