package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mindmatrix.employeetracker.data.model.Employee
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IEmployeeRepository {

    private val collection = firestore.collection("employees")
    private val storageRef = storage.reference.child("profile_images")

    override fun getAllEmployees(): Flow<List<Employee>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val employees = snapshot?.documents?.map { doc ->
                Employee.fromMap(doc.id, doc.data ?: emptyMap())
            }.orEmpty()
            trySend(employees)
        }
        awaitClose { listener.remove() }
    }

    override fun getEmployeeById(id: String): Flow<Employee?> = callbackFlow {
        val listener = collection.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val employee = snapshot?.data?.let { Employee.fromMap(id, it) }
            trySend(employee)
        }
        awaitClose { listener.remove() }
    }

    override fun getEmployeesByDepartment(department: String): Flow<List<Employee>> = callbackFlow {
        val listener = collection.whereEqualTo("department", department).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val employees = snapshot?.documents?.map { doc ->
                Employee.fromMap(doc.id, doc.data ?: emptyMap())
            }.orEmpty()
            trySend(employees)
        }
        awaitClose { listener.remove() }
    }

    override fun getEmployeesByManager(managerId: String): Flow<List<Employee>> = callbackFlow {
        val listener = collection.whereEqualTo("managerId", managerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val employees = snapshot?.documents?.map { doc ->
                Employee.fromMap(doc.id, doc.data ?: emptyMap())
            }.orEmpty()
            trySend(employees)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addEmployee(employee: Employee): Result<String> = try {
        val id = employee.id.ifBlank { collection.document().id }
        val payload = employee.copy(id = id, lastUpdated = System.currentTimeMillis()).toMap() + ("id" to id)
        collection.document(id).set(payload).await()
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateEmployee(employee: Employee): Result<Unit> = try {
        val payload = employee.copy(lastUpdated = System.currentTimeMillis()).toMap() + ("id" to employee.id)
        collection.document(employee.id).set(payload).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteEmployee(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getEmployeeByEmail(email: String): Employee? = try {
        val snapshot = collection.whereEqualTo("email", email).limit(1).get().await()
        snapshot.documents.firstOrNull()?.let { Employee.fromMap(it.id, it.data ?: emptyMap()) }
    } catch (_: Exception) {
        null
    }

    override suspend fun syncEmployees(): Result<Unit> = Result.success(Unit)

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> = try {
        val ref = storageRef.child("$userId.jpg")
        ref.putFile(imageUri).await()
        val url = ref.downloadUrl.await().toString()
        collection.document(userId).update(
            mapOf(
                "profileImageUrl" to url,
                "lastUpdated" to System.currentTimeMillis()
            )
        ).await()
        Result.success(url)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
