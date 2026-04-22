package com.mindmatrix.employeetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.domain.model.Employee
import com.mindmatrix.employeetracker.domain.repository.EmployeeRepository
import com.mindmatrix.employeetracker.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmployeeViewModel(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Employee>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Employee>>> = _state

    fun loadEmployees() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val data = repository.getEmployees()
                _state.value = UiState.Success(data)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error")
            }
        }
    }
}