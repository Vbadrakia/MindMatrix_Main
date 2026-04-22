package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.repository.IEmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

data class EmployeeListState(
    val employees: List<Employee> = emptyList(),
    val filteredEmployees: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedDepartment: String? = null,
    val startDate: String = "",
    val endDate: String = "",
    val error: String? = null
)

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: IEmployeeRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            employeeRepository.syncEmployees()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDepartment = MutableStateFlow<String?>(null)
    private val _startDate = MutableStateFlow("")
    private val _endDate = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<EmployeeListState> = combine(
        employeeRepository.getAllEmployees(),
        _searchQuery,
        _selectedDepartment,
        _startDate,
        _endDate,
        _error
    ) { employees, query, dept, startDate, endDate, error ->
        EmployeeListState(
            employees = employees,
            filteredEmployees = filterEmployees(employees, query, dept, startDate, endDate),
            searchQuery = query,
            selectedDepartment = dept,
            startDate = startDate,
            endDate = endDate,
            error = error,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EmployeeListState(isLoading = true)
    )

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    fun searchEmployees(query: String) {
        _searchQuery.value = query
    }

    fun filterByDepartment(department: String?) {
        _selectedDepartment.value = department
    }

    fun setDateRange(startDate: String, endDate: String) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    private fun filterEmployees(
        employees: List<Employee>,
        query: String,
        department: String?,
        startDate: String,
        endDate: String
    ): List<Employee> {
        val start = parseDate(startDate)
        val end = parseDate(endDate)
        return employees.filter { emp ->
            val matchesQuery = query.isBlank() ||
                emp.name.contains(query, ignoreCase = true) ||
                emp.email.contains(query, ignoreCase = true) ||
                emp.designation.contains(query, ignoreCase = true)
            val matchesDept = department == null || emp.department == department
            val joiningDate = parseDate(emp.joiningDate)
            val matchesStart = start == null || (joiningDate != null && !joiningDate.isBefore(start))
            val matchesEnd = end == null || (joiningDate != null && !joiningDate.isAfter(end))
            matchesQuery && matchesDept && matchesStart && matchesEnd
        }
    }

    private fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return try {
            LocalDate.parse(raw)
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    fun selectEmployee(employee: Employee) {
        _selectedEmployee.value = employee
    }

    fun filterByRole(currentUser: Employee?) {
        if (currentUser == null) return
        when (currentUser.role) {
            com.mindmatrix.employeetracker.data.model.UserRole.ADMIN -> filterByDepartment(null)
            com.mindmatrix.employeetracker.data.model.UserRole.LEAD -> filterByDepartment(currentUser.department)
            com.mindmatrix.employeetracker.data.model.UserRole.EMPLOYEE -> filterByDepartment(null)
        }
    }

    fun loadEmployeeById(id: String) {
        viewModelScope.launch {
            employeeRepository.getEmployeeById(id).collect { employee ->
                _selectedEmployee.value = employee
            }
        }
    }

    fun loadEmployeesByDepartment(department: String) {
        viewModelScope.launch {
            filterByDepartment(department)
        }
    }

    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            val result = employeeRepository.addEmployee(employee)
            result.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            val result = employeeRepository.updateEmployee(employee)
            result.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch {
            val result = employeeRepository.deleteEmployee(id)
            result.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun uploadProfileImage(userId: String, imageUri: android.net.Uri) {
        viewModelScope.launch {
            val result = employeeRepository.uploadProfileImage(userId, imageUri)
            result.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
