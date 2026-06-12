package com.goldleaf.feature.farmermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.local.TaskEntity
import com.goldleaf.feature.cropmanagement.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksListViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksListUiState())
    val uiState: StateFlow<TasksListUiState> = _uiState.asStateFlow()

    fun loadTasks(farmId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            taskRepository.getTasksByFarmIdFlow(farmId).collect { tasks ->
                _uiState.update {
                    it.copy(tasks = tasks, isLoading = false)
                }
            }
        }
    }

    fun toggleTaskComplete(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, isCompleted)
        }
    }
}

data class TasksListUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = false
)
