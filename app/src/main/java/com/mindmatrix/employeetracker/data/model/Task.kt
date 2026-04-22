package com.mindmatrix.employeetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * Represents a task assigned to an employee.
 * Maps to Firestore "tasks" collection and Room "tasks" table.
 */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["employee_id"]), Index(value = ["status"])]
)
data class Task(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "employee_id")
    val assignedTo: String = "",
    val assignedBy: String = "",
    @ColumnInfo(name = "status")
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    @ColumnInfo(name = "deadline")
    val dueDate: String = "",
    val createdAt: String = "",
    val completedAt: String = "",
    val comments: String = "",
    val attachments: List<String> = emptyList(),
    val isPersonalGoal: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "description" to description,
        "assignedTo" to assignedTo,
        "employee_id" to assignedTo,
        "assignedBy" to assignedBy,
        "status" to status.name,
        "priority" to priority.name,
        "dueDate" to dueDate,
        "deadline" to dueDate,
        "createdAt" to createdAt,
        "completedAt" to completedAt,
        "comments" to comments,
        "attachments" to attachments,
        "isPersonalGoal" to isPersonalGoal,
        "lastUpdated" to lastUpdated
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Task = Task(
            id = id,
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            assignedTo = (map["assignedTo"] ?: map["employee_id"]) as? String ?: "",
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
            dueDate = (map["dueDate"] ?: map["deadline"]) as? String ?: "",
            createdAt = map["createdAt"] as? String ?: "",
            completedAt = map["completedAt"] as? String ?: "",
            comments = map["comments"] as? String ?: "",
            attachments = @Suppress("UNCHECKED_CAST") (map["attachments"] as? List<String> ?: emptyList()),
            isPersonalGoal = map["isPersonalGoal"] as? Boolean ?: false,
            lastUpdated = (map["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REVIEWED,
    OVERDUE,
    CANCELLED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
