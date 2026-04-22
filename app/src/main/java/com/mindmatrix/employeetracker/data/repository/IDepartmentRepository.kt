package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.model.Department
import kotlinx.coroutines.flow.Flow

interface IDepartmentRepository {
    fun getAllDepartments(): Flow<List<Department>>
    fun getDepartmentById(id: String): Flow<Department?>
    suspend fun addDepartment(department: Department): Result<String>
    suspend fun updateDepartment(department: Department): Result<Unit>
    suspend fun deleteDepartment(id: String): Result<Unit>
    suspend fun syncDepartments(): Result<Unit>
}
