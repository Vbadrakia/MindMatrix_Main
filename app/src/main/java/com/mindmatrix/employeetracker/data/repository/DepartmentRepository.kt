package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.local.dao.DepartmentDao
import com.mindmatrix.employeetracker.data.model.Department
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val departmentDao: DepartmentDao
) : IDepartmentRepository {
    private val collection = firestore.collection("departments")

    override fun getAllDepartments(): Flow<List<Department>> = departmentDao.getAllDepartments()

    override fun getDepartmentById(id: String): Flow<Department?> = departmentDao.getDepartmentById(id)

    override suspend fun addDepartment(department: Department): Result<String> = try {
        val docRef = if (department.id.isEmpty()) {
            collection.document()
        } else {
            collection.document(department.id)
        }
        val finalId = docRef.id
        val finalDept = department.copy(id = finalId)
        docRef.set(finalDept.toMap()).await()
        departmentDao.insertDepartment(finalDept)
        Result.success(finalId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDepartment(department: Department): Result<Unit> = try {
        collection.document(department.id).set(department.toMap()).await()
        departmentDao.updateDepartment(department)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDepartment(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        departmentDao.deleteDepartmentById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncDepartments(): Result<Unit> = try {
        val snapshot = collection.get().await()
        val departments = snapshot.documents.map { doc ->
            Department.fromMap(doc.id, doc.data ?: emptyMap())
        }
        departmentDao.deleteAllDepartments()
        departmentDao.insertDepartments(departments)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
