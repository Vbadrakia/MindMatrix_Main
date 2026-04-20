package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.local.dao.EmployeeDao
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val employeeDao: EmployeeDao
) : IEmployeeRepository {
    private val collection = firestore.collection("employees")

    override fun getAllEmployees(): Flow<List<Employee>> = employeeDao.getAllEmployees()

    override fun getEmployeeById(id: String): Flow<Employee?> = employeeDao.getEmployeeById(id)

    override fun getEmployeesByDepartment(department: String): Flow<List<Employee>> = 
        employeeDao.getEmployeesByDepartment(department)

    override fun getEmployeesByManager(managerId: String): Flow<List<Employee>> = callbackFlow {
        val listener = collection
            .whereEqualTo("managerId", managerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val employees = snapshot?.documents?.map { doc ->
                    Employee.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(employees)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addEmployee(employee: Employee): Result<String> = try {
        val id = if (employee.id.isNotEmpty()) {
            collection.document(employee.id).set(employee.toMap()).await()
            employee.id
        } else {
            val docRef = collection.add(employee.toMap()).await()
            docRef.id
        }
        val finalEmployee = employee.copy(id = id)
        employeeDao.insertEmployee(finalEmployee)
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateEmployee(employee: Employee): Result<Unit> = try {
        collection.document(employee.id).set(employee.toMap()).await()
        employeeDao.updateEmployee(employee)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteEmployee(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        employeeDao.deleteEmployeeById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getEmployeeByEmail(email: String): Employee? = try {
        val local = employeeDao.getAllEmployees().first().find { it.email == email }
        if (local != null) {
            local
        } else {
            val snapshot = collection
                .whereEqualTo("email", email)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { doc ->
                val employee = Employee.fromMap(doc.id, doc.data ?: emptyMap())
                employeeDao.insertEmployee(employee)
                employee
            }
        }
    } catch (e: Exception) {
        null
    }

    override suspend fun syncEmployees(): Result<Unit> = try {
        val snapshot = collection.get().await()
        val employees = snapshot.documents.map { doc ->
            Employee.fromMap(doc.id, doc.data ?: emptyMap())
        }
        employeeDao.deleteAllEmployees()
        employeeDao.insertEmployees(employees)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
