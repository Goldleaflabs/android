package com.goldleaf.feature.farmermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.feature.cropmanagement.domain.repository.CropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCropsViewModel @Inject constructor(
    private val repository: CropRepository
) : ViewModel() {

    private val _farmId = MutableStateFlow<String?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Reactive flow: whenever _farmId changes, this automatically re-queries Room
    @OptIn(ExperimentalCoroutinesApi::class)
    val crops = _farmId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getAllMyCrops().map { allCrops ->
            // Filter locally by farmId to keep the UI snappy
            allCrops.filter { it.farmId == id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCrops(farmId: String) {
        _farmId.value = farmId
        // Trigger a sync in the background if needed
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncCrops()
            _isLoading.value = false
        }
    }
}