package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.local.dao.TaskDao
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val taskDao: TaskDao
) : ITaskRepository {
    private val collection = firestore.collection("tasks")

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTasksForEmployee(employeeId: String): Flow<List<Task>> = 
        taskDao.getTasksByEmployee(employeeId)

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> = callbackFlow {
        // Fallback to Firestore for specific status queries if not indexed in Room
        val listener = collection
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.map { doc ->
                    Task.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addTask(task: Task): Result<String> = try {
        val docRef = collection.add(task.toMap()).await()
        val finalTask = task.copy(id = docRef.id)
        taskDao.insertTask(finalTask)
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTask(task: Task): Result<Unit> = try {
        collection.document(task.id).set(task.toMap()).await()
        taskDao.updateTask(task)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> = try {
        collection.document(taskId).update("status", status.name).await()
        // Fetch current task and update local Room DB
        val snapshot = collection.document(taskId).get().await()
        snapshot.data?.let { data ->
            val updatedTask = Task.fromMap(taskId, data)
            taskDao.insertTask(updatedTask)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTask(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        taskDao.deleteTaskById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncTasks(): Result<Unit> = try {
        val snapshot = collection.get().await()
        val remoteTasks = snapshot.documents.map {
            Task.fromMap(it.id, it.data ?: emptyMap())
        }

        val localTasks = taskDao.getAllTasks().first()

        remoteTasks.forEach { remote ->
            val local = localTasks.find { it.id == remote.id }
            if (local == null || remote.lastUpdated > local.lastUpdated) {
                taskDao.insertTask(remote)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
