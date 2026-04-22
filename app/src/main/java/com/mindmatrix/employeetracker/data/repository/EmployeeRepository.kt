package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.mindmatrix.employeetracker.data.local.dao.EmployeeDao
import com.mindmatrix.employeetracker.data.model.Employee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
    private val employeeDao: EmployeeDao
) : IEmployeeRepository {
    override fun getAllEmployees(): Flow<List<Employee>> = employeeDao.getAllEmployees()

    override fun getEmployeeById(id: String): Flow<Employee?> = employeeDao.getEmployeeById(id)

    override fun getEmployeesByDepartment(department: String): Flow<List<Employee>> = 
        employeeDao.getEmployeesByDepartment(department)

    override fun getEmployeesByManager(managerId: String): Flow<List<Employee>> =
        employeeDao.getAllEmployees().map { employees ->
            employees.filter { it.managerId == managerId }
        }

    override suspend fun addEmployee(employee: Employee): Result<String> = try {
        val id = employee.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val finalEmployee = employee.copy(id = id, lastUpdated = System.currentTimeMillis())
        employeeDao.insertEmployee(finalEmployee)
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateEmployee(employee: Employee): Result<Unit> = try {
        val timestampedEmployee = employee.copy(lastUpdated = System.currentTimeMillis())
        employeeDao.updateEmployee(timestampedEmployee)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteEmployee(id: String): Result<Unit> = try {
        employeeDao.deleteEmployeeById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getEmployeeByEmail(email: String): Employee? = try {
        employeeDao.getAllEmployees().first().find { it.email == email }
    } catch (e: Exception) {
        null
    }

    override suspend fun syncEmployees(): Result<Unit> = try {
        // Room is the primary source of truth in PRD-aligned offline mode.
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> = try {
        val localUri = imageUri.toString()
        val employee = employeeDao.getEmployeeById(userId).first()
        if (employee != null) {
            val updatedEmployee = employee.copy(profileImageUrl = localUri)
            updateEmployee(updatedEmployee).getOrThrow()
        }
        Result.success(localUri)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
