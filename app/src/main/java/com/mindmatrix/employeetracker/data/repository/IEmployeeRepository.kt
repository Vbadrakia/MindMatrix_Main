package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.mindmatrix.employeetracker.data.model.Employee
import kotlinx.coroutines.flow.Flow

interface IEmployeeRepository {
    fun getAllEmployees(): Flow<List<Employee>>
    fun getEmployeeById(id: String): Flow<Employee?>
    fun getEmployeesByDepartment(department: String): Flow<List<Employee>>
    fun getEmployeesByManager(managerId: String): Flow<List<Employee>>
    suspend fun addEmployee(employee: Employee): Result<String>
    suspend fun updateEmployee(employee: Employee): Result<Unit>
    suspend fun deleteEmployee(id: String): Result<Unit>
    suspend fun getEmployeeByEmail(email: String): Employee?
    suspend fun syncEmployees(): Result<Unit>
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String>
}
