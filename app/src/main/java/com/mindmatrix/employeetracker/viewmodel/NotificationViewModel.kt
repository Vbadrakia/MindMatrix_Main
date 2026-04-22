package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.Notification
import com.mindmatrix.employeetracker.data.repository.INotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            notificationRepository.getNotificationsForUser(userId).collect { notifications ->
                _state.value = _state.value.copy(
                    notifications = notifications,
                    isLoading = false
                )
            }
        }
    }

    fun sendNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.sendNotification(notification).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun clearAll(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            notificationRepository.clearAllNotifications(userId).onSuccess {
                _state.value = _state.value.copy(isLoading = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
