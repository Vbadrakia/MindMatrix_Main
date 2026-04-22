package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.model.Notification
import kotlinx.coroutines.flow.Flow

interface INotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    suspend fun sendNotification(notification: Notification): Result<Unit>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun clearAllNotifications(userId: String): Result<Unit>
}
