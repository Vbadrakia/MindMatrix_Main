package com.mindmatrix.employeetracker.domain.repository

import com.mindmatrix.employeetracker.domain.model.Employee

interface EmployeeRepository {
    suspend fun getEmployees(): List<Employee>
}