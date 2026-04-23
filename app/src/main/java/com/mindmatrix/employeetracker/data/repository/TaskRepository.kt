package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ITaskRepository {
    private val collection = firestore.collection("tasks")

    override fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val listener = collection
            .orderBy("lastUpdated", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    override fun getTasksForEmployee(employeeId: String): Flow<List<Task>> = callbackFlow {
        val listener = collection
            .whereEqualTo("assignedTo", employeeId)
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

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> = callbackFlow {
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
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTask(task: Task): Result<Unit> = try {
        collection.document(task.id).set(task.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> = try {
        collection.document(taskId).update("status", status.name, "lastUpdated", System.currentTimeMillis()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTask(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncTasks(): Result<Unit> = Result.success(Unit) // No longer needed with real-time sync
}
