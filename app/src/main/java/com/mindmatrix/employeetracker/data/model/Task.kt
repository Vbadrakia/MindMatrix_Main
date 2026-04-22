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
    indices = [Index(value = ["employeeId"]), Index(value = ["status"])]
)
data class Task(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val employeeId: String = "",
    val assignedBy: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val deadline: String = "",
    val assignedDate: String = "",
    val completedAt: String = "",
    val comments: String = "",
    val attachments: List<String> = emptyList(),
    val isPersonalGoal: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Compatibility aliases used by existing UI/domain code paths.
    val assignedTo: String get() = employeeId
    val dueDate: String get() = deadline
    val createdAt: String get() = assignedDate

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "employee_id" to employeeId,
        "assignedBy" to assignedBy,
        "status" to status.firestoreValue,
        "priority" to priority.name,
        "deadline" to deadline,
        "assigned_date" to assignedDate,
        // Backward-compatible fields
        "assignedTo" to employeeId,
        "dueDate" to deadline,
        "createdAt" to assignedDate,
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
            employeeId = (map["employee_id"] as? String ?: map["assignedTo"] as? String ?: ""),
            assignedBy = map["assignedBy"] as? String ?: "",
            status = TaskStatus.fromFirestore(map["status"] as? String),
            priority = try {
                TaskPriority.valueOf(map["priority"] as? String ?: "MEDIUM")
            } catch (_: Exception) {
                TaskPriority.MEDIUM
            },
            deadline = (map["deadline"] as? String ?: map["dueDate"] as? String ?: ""),
            assignedDate = (map["assigned_date"] as? String ?: map["createdAt"] as? String ?: ""),
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
    CANCELLED;

    val firestoreValue: String
        get() = when (this) {
            PENDING -> "Pending"
            IN_PROGRESS -> "In Progress"
            COMPLETED -> "Completed"
            REVIEWED -> "Reviewed"
            OVERDUE -> "Overdue"
            CANCELLED -> "Cancelled"
        }

    companion object {
        fun fromFirestore(value: String?): TaskStatus = when (value?.trim()) {
            "Pending", "PENDING" -> PENDING
            "In Progress", "IN_PROGRESS" -> IN_PROGRESS
            "Completed", "COMPLETED" -> COMPLETED
            "Reviewed", "REVIEWED" -> REVIEWED
            "Overdue", "OVERDUE" -> OVERDUE
            "Cancelled", "CANCELLED" -> CANCELLED
            else -> PENDING
        }
    }
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
