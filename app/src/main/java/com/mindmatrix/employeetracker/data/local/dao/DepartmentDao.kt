package com.mindmatrix.employeetracker.data.local.dao

import androidx.room.*
import com.mindmatrix.employeetracker.data.model.Department
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<Department>>

    @Query("SELECT * FROM departments WHERE id = :id")
    fun getDepartmentById(id: String): Flow<Department?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: Department)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<Department>)

    @Update
    suspend fun updateDepartment(department: Department)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartmentById(id: String)

    @Query("DELETE FROM departments")
    suspend fun deleteAllDepartments()
}
