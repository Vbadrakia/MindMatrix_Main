package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.Department
import com.mindmatrix.employeetracker.data.repository.IDepartmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DepartmentState(
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DepartmentViewModel @Inject constructor(
    private val departmentRepository: IDepartmentRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<DepartmentState> = combine(
        departmentRepository.getAllDepartments(),
        _error
    ) { departments, error ->
        DepartmentState(
            departments = departments,
            error = error,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DepartmentState(isLoading = true)
    )

    fun addDepartment(name: String, description: String, headId: String = "") {
        viewModelScope.launch {
            val department = Department(name = name, description = description, headId = headId)
            departmentRepository.addDepartment(department).onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun updateDepartment(department: Department) {
        viewModelScope.launch {
            departmentRepository.updateDepartment(department).onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            departmentRepository.deleteDepartment(id).onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun syncDepartments() {
        viewModelScope.launch {
            departmentRepository.syncDepartments()
        }
    }

    fun clearError() {
        _error.value = null
    }
}
