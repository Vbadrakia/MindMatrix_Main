package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.data.repository.ITaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val selectedStatus: TaskStatus? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: ITaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            taskRepository.getAllTasks().collect { tasks ->
                _state.value = _state.value.copy(
                    tasks = tasks,
                    filteredTasks = filterTasks(tasks, _state.value.searchQuery, _state.value.selectedStatus),
                    isLoading = false
                )
            }
        }
    }

    fun loadTasksForEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            taskRepository.getTasksForEmployee(employeeId).collect { tasks ->
                _state.value = _state.value.copy(
                    tasks = tasks,
                    filteredTasks = filterTasks(tasks, _state.value.searchQuery, _state.value.selectedStatus),
                    isLoading = false
                )
            }
        }
    }

    fun searchTasks(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredTasks = filterTasks(_state.value.tasks, query, _state.value.selectedStatus)
        )
    }

    fun filterByStatus(status: TaskStatus?) {
        _state.value = _state.value.copy(
            selectedStatus = status,
            filteredTasks = filterTasks(_state.value.tasks, _state.value.searchQuery, status)
        )
    }

    private fun filterTasks(tasks: List<Task>, query: String, status: TaskStatus?): List<Task> {
        return tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)
            val matchesStatus = status == null || task.status == status
            matchesQuery && matchesStatus
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskRepository.addTask(task).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, status).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(id).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
