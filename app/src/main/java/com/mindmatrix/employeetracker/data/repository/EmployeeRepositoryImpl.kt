package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.remote.FirebaseService
import com.mindmatrix.employeetracker.domain.model.Employee
import com.mindmatrix.employeetracker.domain.repository.EmployeeRepository

class EmployeeRepositoryImpl(
    private val firebaseService: FirebaseService
) : EmployeeRepository {

    override suspend fun getEmployees(): List<Employee> {
        val snapshot = firebaseService.getEmployees()
        return snapshot.toObjects(Employee::class.java)
    }
}