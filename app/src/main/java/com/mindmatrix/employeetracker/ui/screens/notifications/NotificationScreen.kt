package com.mindmatrix.employeetracker.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.ui.components.DashboardTopBar
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by notificationViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val currentEmployee = authState.currentEmployee
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(currentEmployee) {
        currentEmployee?.let {
            notificationViewModel.loadNotifications(it.id)
        }
    }

        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.notifications),
                subtitle = stringResource(R.string.notifications_subtitle),
                onNotificationClick = { /* Already here */ }
            )
        },
        floatingActionButton = {
            if (state.notifications.isNotEmpty()) {
                SmallFloatingActionButton(
                    onClick = { showClearConfirm = true },
                    containerColor = Error,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.clear_all))
                }
            }
        },
        containerColor = Background
    ) { padding ->
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text(stringResource(R.string.clear_all_notifications)) },
                text = { Text(stringResource(R.string.clear_notifications_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            currentEmployee?.let { notificationViewModel.clearAll(it.id) }
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Error)
                    ) {
                        Text(stringResource(R.string.clear_all))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        if (state.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.no_notifications),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkAsRead = { notificationViewModel.markAsRead(notification.id) },
                        onDelete = { notificationViewModel.deleteNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: com.mindmatrix.employeetracker.data.model.Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Surface else Primary.copy(alpha = 0.05f)
        ),
        border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationTypeColor(notification.type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationTypeIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationTypeColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Column {
                if (!notification.isRead) {
                    IconButton(onClick = onMarkAsRead) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.mark_as_read), tint = Success)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Error)
                }
            }
        }
    }
}

fun getNotificationTypeIcon(type: com.mindmatrix.employeetracker.data.model.NotificationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        com.mindmatrix.employeetracker.data.model.NotificationType.FEEDBACK_REQUEST -> Icons.Default.Chat
        com.mindmatrix.employeetracker.data.model.NotificationType.TASK_ASSIGNED -> Icons.Default.Assignment
        com.mindmatrix.employeetracker.data.model.NotificationType.TASK_COMPLETED -> Icons.Default.TaskAlt
        com.mindmatrix.employeetracker.data.model.NotificationType.PERFORMANCE_ALERT -> Icons.Default.TrendingDown
        com.mindmatrix.employeetracker.data.model.NotificationType.REMINDER -> Icons.Default.Timer
        com.mindmatrix.employeetracker.data.model.NotificationType.GENERAL -> Icons.Default.Notifications
    }
}

fun getNotificationTypeColor(type: com.mindmatrix.employeetracker.data.model.NotificationType): Color {
    return when (type) {
        com.mindmatrix.employeetracker.data.model.NotificationType.FEEDBACK_REQUEST -> Info
        com.mindmatrix.employeetracker.data.model.NotificationType.TASK_ASSIGNED -> Primary
        com.mindmatrix.employeetracker.data.model.NotificationType.TASK_COMPLETED -> Success
        com.mindmatrix.employeetracker.data.model.NotificationType.PERFORMANCE_ALERT -> Error
        com.mindmatrix.employeetracker.data.model.NotificationType.REMINDER -> Warning
        com.mindmatrix.employeetracker.data.model.NotificationType.GENERAL -> Secondary
    }
}
