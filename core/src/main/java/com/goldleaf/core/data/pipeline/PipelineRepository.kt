package com.goldleaf.core.data.pipeline

import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.dto.PipelineStageDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PipelineRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val _stages = MutableStateFlow<List<PipelineStageDto>>(emptyList())
    val stages: StateFlow<List<PipelineStageDto>> = _stages.asStateFlow()

    private val _harvestStageId = MutableStateFlow<Int?>(null)
    val harvestStageId: StateFlow<Int?> = _harvestStageId.asStateFlow()

    suspend fun refresh() {
        val response = apiService.getPipeline()
        if (response.isSuccessful && response.body()?.success == true) {
            val pipelineStages = response.body()!!.data
            _stages.value = pipelineStages
            _harvestStageId.value = pipelineStages.firstOrNull { it.isHarvestStage }?.id
        }
    }

    suspend fun getHarvestStageId(): Int? {
        if (_stages.value.isEmpty()) refresh()
        return _harvestStageId.value
    }

    suspend fun getStages(): List<PipelineStageDto> {
        if (_stages.value.isEmpty()) refresh()
        return _stages.value
    }
}
