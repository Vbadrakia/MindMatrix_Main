package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.UserRole
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
        val listener = collection
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

    override fun getEmployeeById(id: String): Flow<Employee?> = callbackFlow {
        val listener = collection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val employee = snapshot?.let { doc ->
                    if (doc.exists()) Employee.fromMap(doc.id, doc.data ?: emptyMap()) else null
                }
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

    private suspend fun createFirebaseAuthUser(email: String): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = java.net.URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=AIzaSyDIQGtQusevkOEHoHEs3JwoIp-30-e-f5Q")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            
            val jsonInputString = org.json.JSONObject().apply {
                put("email", email)
                put("password", "password123")
                put("returnSecureToken", false)
            }.toString()
            
            connection.outputStream.use { os ->
                val input = jsonInputString.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                org.json.JSONObject(response).getString("localId")
            } else {
                val error = connection.errorStream.bufferedReader().use { it.readText() }
                throw Exception("Auth Failed: ${org.json.JSONObject(error).getJSONObject("error").getString("message")}")
            }
        }
    }

    override suspend fun addEmployee(employee: Employee): Result<String> = try {
        val id = if (employee.id.isNotEmpty()) {
            collection.document(employee.id).set(employee.toMap()).await()
            employee.id
        } else {
            // New user from UI -> Create Firebase Auth profile first
            val authUid = createFirebaseAuthUser(employee.email)
            // Then save Employee data using `authUid` as document ID
            collection.document(authUid).set(employee.toMap().toMutableMap().apply { put("id", authUid) }).await()
            authUid
        }
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
        collection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getEmployeeByEmail(email: String): Employee? = try {
        val snapshot = collection
            .whereEqualTo("email", email)
            .get()
            .await()
        snapshot.documents.firstOrNull()?.let { doc ->
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
        
        // Update employee document with the new photo URL
        collection.document(userId).update("profileImageUrl", downloadUrl).await()
        
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
