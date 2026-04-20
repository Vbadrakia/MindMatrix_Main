package com.mindmatrix.employeetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a task assigned to an employee.
 * Maps to Firestore "tasks" collection and Room "tasks" table.
 */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["assignedTo"]), Index(value = ["status"])]
)
data class Task(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assignedTo: String = "",
    val assignedBy: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: String = "",
    val createdAt: String = "",
    val completedAt: String = "",
    val comments: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "description" to description,
        "assignedTo" to assignedTo,
        "assignedBy" to assignedBy,
        "status" to status.name,
        "priority" to priority.name,
        "dueDate" to dueDate,
        "createdAt" to createdAt,
        "completedAt" to completedAt,
        "comments" to comments
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Task = Task(
            id = id,
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            assignedTo = map["assignedTo"] as? String ?: "",
            assignedBy = map["assignedBy"] as? String ?: "",
            status = try {
                TaskStatus.valueOf(map["status"] as? String ?: "PENDING")
            } catch (_: Exception) {
                TaskStatus.PENDING
            },
            priority = try {
                TaskPriority.valueOf(map["priority"] as? String ?: "MEDIUM")
            } catch (_: Exception) {
                TaskPriority.MEDIUM
            },
            dueDate = map["dueDate"] as? String ?: "",
            createdAt = map["createdAt"] as? String ?: "",
            completedAt = map["completedAt"] as? String ?: "",
            comments = map["comments"] as? String ?: ""
        )
    }
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE,
    CANCELLED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
