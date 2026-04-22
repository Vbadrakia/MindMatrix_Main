package com.mindmatrix.employeetracker.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mindmatrix.employeetracker.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.viewmodel.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun LeadDashboardScreen(
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    insightsViewModel: InsightsViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
    val notificationState by notificationViewModel.state.collectAsStateWithLifecycle()
    
    val currentEmployee = authState.currentEmployee
    val teamMembers = employeeState.employees.filter { it.managerId == currentEmployee?.id }
    val teamTasks = taskState.tasks.filter { task -> teamMembers.any { it.id == task.employeeId } }
    val teamReviews = performanceState.reviews.filter { review -> teamMembers.any { it.id == review.employeeId } }
    val pendingReviews = teamReviews.filter { !it.isApproved }
    val teamLeaderboard = performanceState.leaderboard.filter { entry -> teamMembers.any { it.id == entry.employeeId } }
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            currentEmployee?.let {
                employeeViewModel.loadEmployees()
                taskViewModel.loadTasks()
                performanceViewModel.loadReviews()
                performanceViewModel.loadLeaderboard()
                performanceViewModel.loadAverageScore(it.id)
                insightsViewModel.generateInsights(com.mindmatrix.employeetracker.data.model.UserRole.LEAD, it.id)
                notificationViewModel.loadNotifications(it.id)
            }
            isRefreshing = false
        }
    )

    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentEmployee?.id) {
        currentEmployee?.let {
            employeeViewModel.loadEmployees()
            taskViewModel.loadTasks()
            performanceViewModel.loadReviews()
            performanceViewModel.loadLeaderboard()
            performanceViewModel.loadAverageScore(it.id)
            insightsViewModel.generateInsights(com.mindmatrix.employeetracker.data.model.UserRole.LEAD, it.id)
            notificationViewModel.loadNotifications(it.id)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.team_lead_dashboard),
                subtitle = stringResource(R.string.dept_oversight),
                onNotificationClick = onNavigateToNotifications
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        },
        containerColor = Background
    ) { padding ->
        if (showAddTaskDialog) {
            AddTaskDialog(
                employees = teamMembers,
                onDismiss = { showAddTaskDialog = false },
                onSubmit = { task ->
                    taskViewModel.addTask(task)
                    showAddTaskDialog = false
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(padding).pullRefresh(pullRefreshState)) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter).zIndex(1f),
                contentColor = Primary,
                backgroundColor = Surface
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.greeting_user, currentEmployee?.name?.split(" ")?.firstOrNull() ?: stringResource(R.string.lead)),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        Text(
                            text = stringResource(R.string.managing_members_dept, teamMembers.size, currentEmployee?.department ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }

                item { InsightsCard(insights = insightsState.insights) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AdminStatCard(
                            label = stringResource(R.string.team_size),
                            value = teamMembers.size.toString(),
                            trend = stringResource(R.string.active),
                            trendColor = StatusPresent,
                            onClick = onNavigateToEmployees,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = stringResource(R.string.pending_tasks),
                            value = teamTasks.count { it.status == TaskStatus.PENDING }.toString(),
                            trend = stringResource(R.string.current),
                            trendColor = StatusLate,
                            onClick = onNavigateToTasks,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { PerformanceTrendCard(reviews = teamReviews) }

                item {
                    Text(
                        text = stringResource(R.string.pending_approvals),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (pendingReviews.isNotEmpty()) {
                    items(pendingReviews) { review ->
                        val employeeName = teamMembers.find { it.id == review.employeeId }?.name ?: stringResource(R.string.unknown_employee)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = stringResource(R.string.review_for_format, employeeName), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = stringResource(R.string.score_label_full, review.weightedScore.toInt()), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                                Button(
                                    onClick = { performanceViewModel.approveReview(review.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(stringResource(R.string.approve), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        EmptyState(
                            icon = Icons.Default.CheckCircle,
                            title = stringResource(R.string.all_caught_up),
                            subtitle = stringResource(R.string.no_pending_reviews),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                }

                item {
                    Column {
                        Text(text = stringResource(R.string.team_leaderboard), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDark, modifier = Modifier.padding(bottom = 16.dp))
                        if (teamLeaderboard.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 24.dp)) {
                                items(teamLeaderboard.take(5)) { entry ->
                                    TopPerformerCard(name = entry.employeeName, designation = entry.designation, score = entry.averageScore)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_tasks),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        TextButton(onClick = onNavigateToTasks) {
                            Text(stringResource(R.string.view_all), color = Primary)
                        }
                    }
                }

                if (teamTasks.isNotEmpty()) {
                    items(teamTasks.take(5)) { task ->
                        LeadTaskCard(
                            task = task,
                            onStatusUpdate = { taskId, newStatus -> taskViewModel.updateTaskStatus(taskId, newStatus) },
                            onClick = { onNavigateToTaskDetail(task.id) }
                        )
                    }
                } else {
                    item {
                        EmptyState(
                            icon = Icons.Default.List,
                            title = stringResource(R.string.no_tasks),
                            subtitle = stringResource(R.string.assign_tasks_prompt),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            action = {
                                Button(onClick = { showAddTaskDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.add_task))
                                }
                            }
                        )
                    }
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TaskBadge(value = getLocalizedTaskPriority(task.priority), color = getTaskPriorityColor(task.priority))
                }
                Box {
                    StatusChip(
                        text = getLocalizedTaskStatus(task.status),
                        color = getTaskStatusColor(task.status),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { showMenu = true }
                    )
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        TaskStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = getLocalizedTaskStatus(status),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.due_date_format, task.dueDate), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
        }
    }
}


