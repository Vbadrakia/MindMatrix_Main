package com.mindmatrix.employeetracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.viewmodel.AttendanceViewModel
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.InsightsViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.mindmatrix.employeetracker.data.model.UserRole
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun EmployeeDashboardScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    insightsViewModel: InsightsViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    
    val currentEmployee = authState.currentEmployee
    var selectedTab by remember { mutableIntStateOf(1) } // 0 for Tasks, 1 for Performance

    LaunchedEffect(currentEmployee?.id) {
        currentEmployee?.let {
            taskViewModel.loadTasksForEmployee(it.id)
            insightsViewModel.generateInsights(UserRole.EMPLOYEE, it.id)
            performanceViewModel.loadReviewsForEmployee(it.id)
            performanceViewModel.loadAverageScore(it.id)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = if (selectedTab == 1) "Performance" else "My Tasks",
                subtitle = currentEmployee?.name ?: "Employee Dashboard",
                onNotificationClick = {}
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Card
            item {
                ProfileHeaderCard(
                    name = currentEmployee?.name ?: "Sarah Jenkins",
                    designation = currentEmployee?.designation ?: "Senior Frontend Developer",
                    department = currentEmployee?.department ?: "Design Systems Team",
                    onViewProfileClick = { currentEmployee?.let { onNavigateToTasks() } } // Or navigate to a specific profile view if available
                )
            }

            // Insights Section
            if (insightsState.insights.isNotEmpty()) {
                item {
                    InsightsCard(insights = insightsState.insights)
                }
            }

            // Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TabItem(label = "Tasks", isSelected = selectedTab == 0, onClick = { selectedTab = 0 })
                    TabItem(label = "Performance", isSelected = selectedTab == 1, onClick = { selectedTab = 1 })
                }
            }

            if (selectedTab == 1) {
                // Annual Rating Metrics Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Annual Rating\nMetrics",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = "Latest Performance Evaluation",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("%.1f", performanceState.averageScore),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = Primary
                            )
                            Text(
                                text = "OVERALL\nSCORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Metric Cards for the latest review
                val latestReview = performanceState.reviews.maxByOrNull { it.reviewDate }
                if (latestReview != null) {
                    item {
                        MetricCard(
                            title = "Quality of Work",
                            score = latestReview.qualityScore.toFloat(),
                            icon = Icons.Default.Diamond,
                            description = "Focus on code quality and attention to detail in your deliveries.",
                            color = Success
                        )
                    }
                    item {
                        MetricCard(
                            title = "Timeliness",
                            score = latestReview.timelinessScore.toFloat(),
                            icon = Icons.Default.Timer,
                            description = "Meeting deadlines and sprint commitments reliably.",
                            color = Primary
                        )
                    }
                    item {
                        MetricCard(
                            title = "Communication",
                            score = latestReview.communicationScore.toFloat(),
                            icon = Icons.Default.ChatBubble,
                            description = "Clarity in updates and effective collaboration with the team.",
                            color = Primary
                        )
                    }
                    item {
                        MetricCard(
                            title = "Innovation",
                            score = latestReview.innovationScore.toFloat(),
                            icon = Icons.Default.Lightbulb,
                            description = "Bringing new ideas and optimizing existing workflows.",
                            color = Success
                        )
                    }
                } else {
                    item {
                        EmptyState(
                            icon = Icons.Default.Assessment,
                            title = "No reviews yet",
                            subtitle = "Your performance metrics will appear here once your lead completes your first evaluation."
                        )
                    }
                }
            } else {
                if (taskState.tasks.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.AssignmentLate,
                            title = "All caught up!",
                            subtitle = "You don't have any tasks assigned at the moment. Take a break or check in with your lead."
                        )
                    }
                } else {
                    items(taskState.tasks) { task ->
                        TaskSmallCard(
                            task = task,
                            onClick = { onNavigateToTaskDetail(task.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}


@Composable
fun ProfileHeaderCard(
    name: String, 
    designation: String, 
    department: String,
    onViewProfileClick: () -> Unit = {}
) {
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
            Box(contentAlignment = Alignment.BottomEnd) {
                EmployeeAvatar(name = name, size = 90)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(StatusPresent)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
            Text(
                text = "$designation • $department",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewProfileClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                FilledTonalIconButton(
                    onClick = { },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = PrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Mail, contentDescription = "Message", tint = Primary)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    text = "Top Performer", 
                    icon = Icons.Default.TrendingUp, 
                    color = SuccessContainer, 
                    textColor = Success
                )
                StatusBadge(
                    text = "Tenure: 3 Yrs", 
                    icon = Icons.Default.Schedule, 
                    color = SurfaceVariant, 
                    textColor = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, icon: ImageVector, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = textColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) Primary else OnSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(Primary)
            )
        } else {
            Box(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun MetricCard(title: String, score: Float, icon: ImageVector, description: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Text(
                    text = score.toString(), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black, 
                    color = PrimaryDark
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = Outline.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description, 
                style = MaterialTheme.typography.bodyMedium, 
                color = OnSurfaceVariant, 
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
