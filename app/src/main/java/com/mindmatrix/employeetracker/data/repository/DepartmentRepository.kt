package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.model.Department
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IDepartmentRepository {
    private val collection = firestore.collection("departments")

    override fun getAllDepartments(): Flow<List<Department>> = callbackFlow {
        val listener = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val departments = snapshot?.documents?.map { doc ->
                    Department.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(departments)
            }
        awaitClose { listener.remove() }
    }

    override fun getDepartmentById(id: String): Flow<Department?> = callbackFlow {
        val listener = collection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val department = snapshot?.let { doc ->
                    if (doc.exists()) Department.fromMap(doc.id, doc.data ?: emptyMap()) else null
                }
                trySend(department)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addDepartment(department: Department): Result<String> = try {
        val docRef = if (department.id.isEmpty()) {
            collection.document()
        } else {
            collection.document(department.id)
        }
        val finalId = docRef.id
        val finalDept = department.copy(id = finalId)
        docRef.set(finalDept.toMap()).await()
        Result.success(finalId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDepartment(department: Department): Result<Unit> = try {
        collection.document(department.id).set(department.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDepartment(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncDepartments(): Result<Unit> = Result.success(Unit)
}
