package com.mindmatrix.employeetracker.data.model

/**
 * Represents a notification in the system.
 * Used for feedback requests, task reminders, and performance alerts.
 */
data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val timestamp: String = "",
    val isRead: Boolean = false,
    val relatedId: String? = null // ID of task, review, etc.
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "recipientId" to recipientId,
        "senderId" to senderId,
        "senderName" to senderName,
        "title" to title,
        "message" to message,
        "type" to type.name,
        "timestamp" to timestamp,
        "isRead" to isRead,
        "relatedId" to relatedId
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Notification = Notification(
            id = id,
            recipientId = map["recipientId"] as? String ?: "",
            senderId = map["senderId"] as? String ?: "",
            senderName = map["senderName"] as? String ?: "",
            title = map["title"] as? String ?: "",
            message = map["message"] as? String ?: "",
            type = try {
                NotificationType.valueOf(map["type"] as? String ?: "GENERAL")
            } catch (_: Exception) {
                NotificationType.GENERAL
            },
            timestamp = map["timestamp"] as? String ?: "",
            isRead = map["isRead"] as? Boolean ?: false,
            relatedId = map["relatedId"] as? String
        )
    }
}

enum class NotificationType {
    GENERAL,
    FEEDBACK_REQUEST,
    TASK_ASSIGNED,
    TASK_COMPLETED,
    PERFORMANCE_ALERT,
    REMINDER
}
