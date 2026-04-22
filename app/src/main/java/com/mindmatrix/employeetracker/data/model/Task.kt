package com.mindmatrix.employeetracker.data.model


/**
 * Represents a task assigned to an employee.
 * Maps to Firestore "tasks" collection.
 */
data class Task(
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
