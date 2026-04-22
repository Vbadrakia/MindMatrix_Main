package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    private val storage: FirebaseStorage,
    private val employeeDao: EmployeeDao
) : IEmployeeRepository {
    private val collection = firestore.collection("employees")
    private val storageRef = storage.reference.child("profile_images")

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
        val remoteEmployees = snapshot.documents.map {
            Employee.fromMap(it.id, it.data ?: emptyMap())
        }

        val localEmployees = employeeDao.getAllEmployees().first()
        
        remoteEmployees.forEach { remote ->
            val local = localEmployees.find { it.id == remote.id }
            if (local == null || remote.lastUpdated > local.lastUpdated) {
                employeeDao.insertEmployee(remote)
            }
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> = try {
        val ref = storageRef.child("$userId.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        
        // Update employee document with the new photo URL
        val employee = employeeDao.getEmployeeById(userId).first()
        if (employee != null) {
            val updatedEmployee = employee.copy(profileImageUrl = downloadUrl)
            updateEmployee(updatedEmployee).getOrThrow()
        }
        
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
