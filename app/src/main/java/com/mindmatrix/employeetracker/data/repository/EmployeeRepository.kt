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
            } ?: emptyList()
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
            val employee = snapshot?.data?.let { Employee.fromMap(snapshot.id, it) }
            trySend(employee)
        }
        awaitClose { listener.remove() }
    }

    override fun getEmployeesByDepartment(department: String): Flow<List<Employee>> = callbackFlow {
        val listener = collection
            .whereEqualTo("department", department)
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
        val docRef = if (employee.id.isNotBlank()) collection.document(employee.id) else collection.document()
        val id = docRef.id
        val finalEmployee = employee.copy(id = id)
        docRef.set(finalEmployee.toMap()).await()
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateEmployee(employee: Employee): Result<Unit> = try {
        collection.document(employee.id).set(employee.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteEmployee(id: String): Result<Unit> = try {
        collection.document(id).update("isActive", false).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getEmployeeByEmail(email: String): Employee? = try {
        val normalized = email.trim().lowercase()
        val snapshot = collection
            .whereEqualTo("email", normalized)
            .get()
            .await()
        (snapshot.documents.firstOrNull()
            ?: collection.whereEqualTo("email", email.trim()).get().await().documents.firstOrNull()
            )?.let { doc ->
            Employee.fromMap(doc.id, doc.data ?: emptyMap())
        }
    } catch (e: Exception) {
        null
    }

    override suspend fun syncEmployees(): Result<Unit> = Result.success(Unit)

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> = try {
        val ref = storageRef.child("$userId.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        
        collection.document(userId).update("profileImageUrl", downloadUrl).await()
        
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
