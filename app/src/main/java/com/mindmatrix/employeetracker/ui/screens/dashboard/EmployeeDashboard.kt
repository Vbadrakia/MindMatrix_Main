package com.mindmatrix.employeetracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.employeetracker.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.viewmodel.*
import com.mindmatrix.employeetracker.data.model.UserRole
import kotlinx.coroutines.launch
import android.widget.Toast
import java.util.Locale

@Composable
fun EmployeeDashboardScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    insightsViewModel: InsightsViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToEmployeeDetail: (String) -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val currentEmployee = authState.currentEmployee
    var selectedTab by remember { mutableIntStateOf(1) } // 0 for Tasks, 1 for Performance
    var showAddGoalDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentEmployee?.id) {
        currentEmployee?.let {
            taskViewModel.loadTasksForEmployee(it.id)
            insightsViewModel.generateInsights(UserRole.EMPLOYEE, it.id)
            performanceViewModel.loadReviewsForEmployee(it.id)
            performanceViewModel.loadAverageScore(it.id)
            performanceViewModel.loadLeaderboard()
            performanceViewModel.loadDepartmentAverages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.employee_dashboard),
                subtitle = stringResource(R.string.personal_performance),
                onNotificationClick = onNavigateToNotifications
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.set_personal_goal))
                }
            }
        },
        containerColor = Background
    ) { padding ->
        if (showAddGoalDialog && currentEmployee != null) {
            AddTaskDialog(
                employees = listOf(currentEmployee),
                onDismiss = { showAddGoalDialog = false },
                onSubmit = { task ->
                    taskViewModel.addTask(task.copy(isPersonalGoal = true))
                    showAddGoalDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.goal_set_success))
                    }
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.greeting_user, currentEmployee?.name?.split(" ")?.firstOrNull() ?: stringResource(R.string.user)),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Text(
                        text = stringResource(R.string.personal_performance_tracking),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            item {
                ProfileHeaderCard(
                    name = currentEmployee?.name ?: stringResource(R.string.user),
                    designation = currentEmployee?.designation ?: "",
                    department = currentEmployee?.department ?: "",
                    onPerformanceClick = onNavigateToPerformance
                )
            }

            if (insightsState.insights.isNotEmpty()) {
                item {
                    InsightsCard(insights = insightsState.insights)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TabItem(text = stringResource(R.string.tasks), selected = selectedTab == 0, onClick = { selectedTab = 0 })
                    TabItem(text = stringResource(R.string.performance), selected = selectedTab == 1, onClick = { selectedTab = 1 })
                }
            }

            if (selectedTab == 1) {
                item {
                    Text(
                        text = stringResource(R.string.predictive_insights),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                val deptAvg = performanceState.departmentAverages.find { it.department == currentEmployee?.department }?.averageScore ?: 0.0
                item {
                    PerformanceComparisonCard(
                        myScore = performanceState.averageScore.toFloat(),
                        deptAverage = deptAvg.toFloat()
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SecondaryDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = null, tint = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.request_feedback),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryDark
                                )
                                Text(
                                    text = stringResource(R.string.ask_lead_checkin),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    currentEmployee?.let { emp ->
                                        if (emp.managerId.isNotEmpty()) {
                                            val notification = com.mindmatrix.employeetracker.data.model.Notification(
                                                recipientId = emp.managerId,
                                                senderId = emp.id,
                                                senderName = emp.name,
                                                title = context.getString(R.string.feedback_requested_title),
                                                message = context.getString(R.string.performance_review_request_msg, emp.name),
                                                type = com.mindmatrix.employeetracker.data.model.NotificationType.FEEDBACK_REQUEST,
                                                timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                                                relatedId = emp.id
                                            )
                                            notificationViewModel.sendNotification(notification)
                                            scope.launch {
                                                Toast.makeText(context, context.getString(R.string.feedback_request_sent), Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            scope.launch {
                                                Toast.makeText(context, context.getString(R.string.no_lead_assigned), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(stringResource(R.string.request))
                            }
                        }
                    }
                }

                val latestReview = performanceState.reviews.maxByOrNull { it.reviewDate }
                if (latestReview != null) {
                    item { MetricCard(label = stringResource(R.string.productivity), score = latestReview.productivityScore.toFloat(), icon = Icons.Default.Speed, description = stringResource(R.string.productivity_desc), color = Primary) }
                    item { MetricCard(label = stringResource(R.string.attendance), score = latestReview.attendanceScore.toFloat(), icon = Icons.Default.EventAvailable, description = stringResource(R.string.attendance_desc), color = Tertiary) }
                    item { MetricCard(label = stringResource(R.string.quality_of_work), score = latestReview.qualityScore.toFloat(), icon = Icons.Default.Verified, description = stringResource(R.string.quality_of_work_desc), color = Success) }
                    item { MetricCard(label = stringResource(R.string.soft_skills), score = latestReview.softSkillsScore.toFloat(), icon = Icons.Default.Psychology, description = stringResource(R.string.soft_skills_desc), color = SecondaryDark) }
                } else {
                    item {
                        EmptyState(
                            icon = Icons.Default.Assessment,
                            title = stringResource(R.string.no_reviews_yet),
                            subtitle = stringResource(R.string.no_reviews_desc)
                        )
                    }
                }
            } else {
                val personalGoals = taskState.tasks.filter { it.isPersonalGoal }
                val assignedTasks = taskState.tasks.filter { !it.isPersonalGoal }

                if (taskState.tasks.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.AssignmentLate,
                            title = stringResource(R.string.all_caught_up),
                            subtitle = stringResource(R.string.no_tasks_goals)
                        )
                    }
                } else {
                    if (personalGoals.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.personal_goals_header),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(personalGoals) { goal ->
                            GoalCard(goal = goal, onClick = { onNavigateToTaskDetail(goal.id) })
                        }
                    }

                    if (assignedTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.assigned_tasks_header),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(assignedTasks) { task ->
                            TaskSmallCard(task = task, onClick = { onNavigateToTaskDetail(task.id) })
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ProfileHeaderCard(name: String, designation: String, department: String, onPerformanceClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmployeeAvatar(name = name, size = 90)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryDark)
            Text(text = stringResource(R.string.designation_dept_format, designation, department), style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPerformanceClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.performance_history), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = stringResource(R.string.top_performer), color = Success)
                StatusBadge(status = stringResource(R.string.tenure_years_format, 3), color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
fun MetricCard(label: String, score: Float, icon: ImageVector, description: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(text = stringResource(R.string.score_format, score), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = color)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun GoalCard(goal: com.mindmatrix.employeetracker.data.model.Task, onClick: () -> Unit = {}) {
    val progress = when (goal.status) {
        com.mindmatrix.employeetracker.data.model.TaskStatus.COMPLETED -> 1f
        com.mindmatrix.employeetracker.data.model.TaskStatus.IN_PROGRESS -> 0.5f
        else -> 0f
    }
    val progressColor = if (goal.status == com.mindmatrix.employeetracker.data.model.TaskStatus.COMPLETED) Success else Primary

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(R.string.target_date_format, goal.dueDate), style = MaterialTheme.typography.bodySmall)
                }
                StatusChip(text = getLocalizedTaskStatus(goal.status), color = getTaskStatusColor(goal.status))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.goal_progress), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.percentage_format, (progress * 100).toInt()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = progressColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = progressColor)
        }
    }
}

@Composable
fun PerformanceComparisonCard(myScore: Float, deptAverage: Float) {
    val difference = myScore - deptAverage
    val comparisonText = if (difference >= 0) {
        stringResource(R.string.above_avg_msg, difference.toInt())
    } else {
        stringResource(R.string.below_avg_msg, (-difference).toInt())
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = stringResource(R.string.performance_benchmarking), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp * (myScore / 100f)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(Primary))
                    Text(text = stringResource(R.string.you), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = myScore.toInt().toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp * (deptAverage / 100f)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(SecondaryDark.copy(alpha = 0.5f)))
                    Text(text = stringResource(R.string.dept_avg_label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = deptAverage.toInt().toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Surface(color = (if (difference >= 0) SuccessContainer else TertiaryContainer).copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(imageVector = if (difference >= 0) Icons.Default.ThumbUp else Icons.Default.Info, contentDescription = null, tint = if (difference >= 0) Success else Tertiary)
                    Text(text = comparisonText, style = MaterialTheme.typography.bodySmall, color = if (difference >= 0) Success else Tertiary)
                }
            }
        }
    }
}
