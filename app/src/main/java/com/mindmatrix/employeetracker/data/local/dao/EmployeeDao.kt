package com.mindmatrix.employeetracker.data.local.dao

import androidx.room.*
import com.mindmatrix.employeetracker.data.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeById(id: String): Flow<Employee?>

    @Query("SELECT * FROM employees WHERE department = :department")
    fun getEmployeesByDepartment(department: String): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)

    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()
}
