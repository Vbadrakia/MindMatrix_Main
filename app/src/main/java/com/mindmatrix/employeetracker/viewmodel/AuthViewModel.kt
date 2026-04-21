package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.data.repository.IEmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentEmployee: Employee? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val employeeRepository: IEmployeeRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                _authState.value = _authState.value.copy(isLoading = true)
                try {
                    val employee = employeeRepository.getEmployeeByEmail(firebaseUser.email ?: "")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoggedIn = employee != null,
                        currentEmployee = employee,
                        error = if (employee == null) "Employee profile not found" else null
                    )
                } catch (e: Exception) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Session check failed: ${e.localizedMessage}"
                    )
                }
            }
        } else {
            // Ensure we are not in loading state if no user
            _authState.value = _authState.value.copy(isLoading = false, isLoggedIn = false)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val normalizedEmail = email.trim().lowercase()
            
            // Hardcoded credentials for development/testing
            if ((normalizedEmail == "admin@mindmatrix.com" || normalizedEmail == "admin@example.com") && (password == "admin123" || password == "password123")) {
                val adminEmployee = Employee(
                    id = "admin_id",
                    email = normalizedEmail,
                    name = "Admin User",
                    role = UserRole.ADMIN,
                    department = "Management",
                    designation = "System Administrator"
                )
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentEmployee = adminEmployee,
                    error = null
                )
                return@launch
            }

            if ((normalizedEmail == "lead@mindmatrix.com" || normalizedEmail == "lead@example.com") && (password == "lead123" || password == "password123")) {
                val leadEmployee = Employee(
                    id = "lead_id",
                    email = normalizedEmail,
                    name = "Team Lead",
                    role = UserRole.LEAD,
                    department = "Engineering",
                    designation = "Senior Lead"
                )
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentEmployee = leadEmployee,
                    error = null
                )
                return@launch
            }

            if ((normalizedEmail == "employee@mindmatrix.com" || normalizedEmail == "employee@example.com") && (password == "employee123" || password == "password123")) {
                val employee = Employee(
                    id = "emp_id",
                    email = normalizedEmail,
                    name = "John Doe",
                    role = UserRole.EMPLOYEE,
                    department = "Engineering",
                    designation = "Software Engineer"
                )
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentEmployee = employee,
                    error = null
                )
                return@launch
            }

            try {
                auth.signInWithEmailAndPassword(normalizedEmail, password)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            val employee = employeeRepository.getEmployeeByEmail(email)
                            if (employee != null) {
                                _authState.value = _authState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    currentEmployee = employee,
                                    error = null
                                )
                            } else {
                                _authState.value = _authState.value.copy(
                                    isLoading = false,
                                    error = "Employee profile not found. Contact your administrator."
                                )
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = exception.localizedMessage ?: "Authentication failed"
                        )
                    }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Authentication failed"
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.localizedMessage ?: "Failed to send reset email") }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
