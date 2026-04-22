package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.model.Notification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : INotificationRepository {

    private val notificationCollection = firestore.collection("notifications")

    override fun getNotificationsForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = notificationCollection
            .whereEqualTo("recipientId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.map { doc ->
                    Notification.fromMap(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                trySend(notifications)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendNotification(notification: Notification): Result<Unit> = try {
        notificationCollection.add(notification.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = try {
        notificationCollection.document(notificationId).update("isRead", true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> = try {
        notificationCollection.document(notificationId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun clearAllNotifications(userId: String): Result<Unit> = try {
        val snapshot = notificationCollection.whereEqualTo("recipientId", userId).get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
