package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.local.dao.TaskDao
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) : ITaskRepository {
    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTasksForEmployee(employeeId: String): Flow<List<Task>> = 
        taskDao.getTasksByEmployee(employeeId)

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> =
        taskDao.getAllTasks().map { tasks -> tasks.filter { it.status == status } }

    override suspend fun addTask(task: Task): Result<String> = try {
        val timestampedTask = task.copy(lastUpdated = System.currentTimeMillis())
        val finalTask = timestampedTask.copy(id = task.id.ifBlank { java.util.UUID.randomUUID().toString() })
        taskDao.insertTask(finalTask)
        Result.success(finalTask.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTask(task: Task): Result<Unit> = try {
        val timestampedTask = task.copy(lastUpdated = System.currentTimeMillis())
        taskDao.updateTask(timestampedTask)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> = try {
        val currentTask = taskDao.getTaskById(taskId).first() ?: return Result.failure(
            IllegalArgumentException("Task not found: $taskId")
        )
        taskDao.updateTask(currentTask.copy(status = status, lastUpdated = System.currentTimeMillis()))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTask(id: String): Result<Unit> = try {
        taskDao.deleteTaskById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncTasks(): Result<Unit> = try {
        // Room-only mode: no remote sync required.
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
