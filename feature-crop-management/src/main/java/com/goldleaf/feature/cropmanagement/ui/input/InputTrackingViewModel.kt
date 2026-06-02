package com.goldleaf.feature.cropmanagement.ui.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.local.InputEntity
import com.goldleaf.core.data.local.InputType
import com.goldleaf.core.data.local.dao.InputDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InputTrackingViewModel @Inject constructor(
    private val inputDao: InputDao
) : ViewModel() {

    private val _inputs = MutableStateFlow<List<InputEntity>>(emptyList())
    val inputs: StateFlow<List<InputEntity>> = _inputs.asStateFlow()

    fun loadInputs(cropId: String) {
        viewModelScope.launch {
            inputDao.getInputsByCrop(cropId).collect { _inputs.value = it }
        }
    }

    fun addInput(
        cropId: String,
        farmId: String,
        farmerId: String,
        type: InputType,
        name: String,
        quantity: Double,
        unit: String,
        applicationDate: String,
        cost: Double,
        supplier: String,
        notes: String
    ) {
        viewModelScope.launch {
            val input = InputEntity(
                id = UUID.randomUUID().toString(),
                cropId = cropId,
                farmId = farmId,
                farmerId = farmerId,
                type = type,
                name = name,
                quantity = quantity,
                unit = unit,
                applicationDate = applicationDate,
                cost = cost,
                supplier = supplier,
                notes = notes
            )
            inputDao.insertInput(input)
        }
    }

    fun deleteInput(input: InputEntity) {
        viewModelScope.launch { inputDao.deleteInput(input) }
    }
}
