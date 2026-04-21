package com.mindmatrix.employeetracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.InsightsViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LeadDashboardScreen(
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    insightsViewModel: InsightsViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
    val currentEmployee = authState.currentEmployee

    LaunchedEffect(currentEmployee) {
        currentEmployee?.let { user ->
            insightsViewModel.generateInsights(UserRole.LEAD, user.id)
            performanceViewModel.loadLeaderboard()
        }
    }

    val isRefreshing by taskViewModel.isRefreshing.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            currentEmployee?.let { user ->
                taskViewModel.loadTasksForLead(user.id)
                employeeViewModel.loadEmployeesByDepartment(user.department)
                insightsViewModel.generateInsights(UserRole.LEAD, user.id)
                performanceViewModel.loadLeaderboard()
            }
        }
    )

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Lead Dashboard",
                subtitle = "Team Overview & Analytics",
                onNotificationClick = { /* Handle notifications */ }
            )
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f),
                contentColor = Primary,
                backgroundColor = Surface
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Welcome Header
            item {
                Column {
                    Text(
                        text = "Hello, ${currentEmployee?.name?.split(" ")?.firstOrNull() ?: "Lead"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Text(
                        text = "Managing ${employeeState.employees.size} Team Members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Insights Card
            item {
                InsightsCard(insights = insightsState.insights)
            }

            // Stat Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        label = "Team Size",
                        value = "${employeeState.employees.size}",
                        trend = "+2",
                        trendColor = StatusPresent,
                        onClick = onNavigateToEmployees,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Pending Tasks",
                        value = "${taskState.tasks.count { it.status == TaskStatus.PENDING }}",
                        trend = "High",
                        trendColor = StatusLate,
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Performance Trend
            item {
                PerformanceTrendCard()
            }

            // Team Tasks Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Team Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    TextButton(onClick = onNavigateToTasks) {
                        Text("View All", color = Primary)
                    }
                }
            }

            // Task Items
            items(taskState.tasks.take(5)) { task ->
                LeadTaskCard(
                    task = task,
                    onStatusUpdate = { taskId, newStatus ->
                        taskViewModel.updateTaskStatus(taskId, newStatus)
                    },
                    onClick = { onNavigateToTaskDetail(task.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
}

@Composable
fun LeadTaskCard(
    task: Task,
    onStatusUpdate: (String, TaskStatus) -> Unit = { _, _ -> },
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { /* Handle click */ }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Text(
                        text = "Priority: ${task.priority.name.lowercase().replaceFirstChar { it.titlecase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = getTaskPriorityColor(task.priority),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box {
                    StatusChip(
                        text = task.status.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
                        color = getTaskStatusColor(task.status),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                onClick = { showMenu = true },
                                onClickLabel = "Change task status"
                            )
                            .minimumInteractiveComponentSize()
                    )
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        TaskStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = status.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    ) 
                                },
                                onClick = {
                                    onStatusUpdate(task.id, status)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(status) {
                                            TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                                            TaskStatus.REVIEWED -> Icons.Default.Verified
                                            TaskStatus.IN_PROGRESS -> Icons.Default.HourglassEmpty
                                            TaskStatus.PENDING -> Icons.Default.Schedule
                                            TaskStatus.OVERDUE -> Icons.Default.Error
                                            TaskStatus.CANCELLED -> Icons.Default.Cancel
                                        },
                                        contentDescription = null,
                                        tint = getTaskStatusColor(status),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (task.status == TaskStatus.COMPLETED) {
                Surface(
                    color = Accent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.PendingActions,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Accent
                        )
                        Text(
                            text = "Awaiting Review",
                            style = MaterialTheme.typography.labelSmall,
                            color = Accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
        }
    }
}
