package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksForEmployee(employeeId: String): Flow<List<Task>>
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>
    suspend fun addTask(task: Task): Result<String>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
    suspend fun deleteTask(id: String): Result<Unit>
    suspend fun syncTasks(): Result<Unit>
}
