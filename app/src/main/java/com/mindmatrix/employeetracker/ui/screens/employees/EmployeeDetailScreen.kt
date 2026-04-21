package com.mindmatrix.employeetracker.ui.screens.employees

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import java.util.Locale
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
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.ui.components.AddPerformanceReviewDialog
import com.mindmatrix.employeetracker.ui.screens.performance.PerformanceReviewCard

@Composable
fun EmployeeDetailScreen(
    employeeId: String,
    onNavigateBack: () -> Unit = {},
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val selectedEmployee by employeeViewModel.selectedEmployee.collectAsState()
    val taskState by taskViewModel.state.collectAsState()
    val performanceState by performanceViewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val currentUser = authState.currentEmployee
    val isAdmin = currentUser?.role == com.mindmatrix.employeetracker.data.model.UserRole.ADMIN
    val isLead = currentUser?.role == com.mindmatrix.employeetracker.data.model.UserRole.LEAD
    val isOwnProfile = currentUser?.id == employeeId

    LaunchedEffect(employeeId) {
        employeeViewModel.loadEmployeeById(employeeId)
        taskViewModel.loadTasksForEmployee(employeeId)
        performanceViewModel.loadReviewsForEmployee(employeeId)
        performanceViewModel.loadAverageScore(employeeId)
    }

    val employee = selectedEmployee
    var showReviewDialog by remember { mutableStateOf(false) }

    if (showReviewDialog && employee != null) {
        AddPerformanceReviewDialog(
            employeeName = employee.name,
            onDismiss = { showReviewDialog = false },
            onSubmit = { quality, timeliness, attendance, communication, innovation, comments, period, overall ->
                val review = com.mindmatrix.employeetracker.data.model.PerformanceReview(
                    employeeId = employee.id,
                    reviewerId = currentUser?.id ?: "",
                    reviewDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                    period = period,
                    qualityScore = quality,
                    timelinessScore = timeliness,
                    attendanceScore = attendance,
                    communicationScore = communication,
                    innovationScore = innovation,
                    rawScore = overall,
                    weightedScore = overall, // Assuming equal weight for now as per dialog logic
                    comments = comments
                )
                performanceViewModel.addReview(review)
                showReviewDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Employee Profile",
                onBackClick = onNavigateBack,
                actions = {
                    if (isAdmin) {
                        IconButton(
                            onClick = { /* Edit logic */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit employee profile")
                        }
                    }
                }
            )
        },
        containerColor = Background
    ) { padding ->
        if (employee == null) {
            LoadingOverlay(isLoading = true, modifier = Modifier.padding(padding))
        } else {
            // Check access: Leads can only view employees in their own department
            val hasAccess = isAdmin || (isLead && employee.department == currentUser?.department) || isOwnProfile
            
            if (!hasAccess) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("You do not have permission to view this profile.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Profile header Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EmployeeAvatar(name = employee.name, size = 80)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = employee.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                                Text(
                                    text = employee.designation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OnSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(
                                        text = employee.department,
                                        color = Primary
                                    )
                                    StatusChip(
                                        text = employee.role.name.lowercase().replaceFirstChar { it.titlecase() },
                                        color = SecondaryDark
                                    )
                                }

                                if (isLead && !isOwnProfile && employee.department == currentUser?.department) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { showReviewDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.RateReview, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Performance Review")
                                    }
                                }
                            }
                        }
                    }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Assigned Tasks",
                            value = "${taskState.tasks.size}",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            gradientColors = listOf(Primary, PrimaryLight),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Performance",
                            value = String.format(Locale.getDefault(), "%.1f", performanceState.averageScore),
                            icon = Icons.Default.Star,
                            gradientColors = listOf(SecondaryDark, Secondary),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Information Section Header
                item {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // Contact info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = employee.email
                            )
                            if (employee.phone.isNotBlank()) {
                                InfoRow(
                                    icon = Icons.Default.Phone,
                                    label = "Phone Number",
                                    value = employee.phone
                                )
                            }
                            if (employee.joinDate.isNotBlank()) {
                                InfoRow(
                                    icon = Icons.Default.CalendarToday,
                                    label = "Joining Date",
                                    value = employee.joinDate
                                )
                            }
                        }
                    }
                }

                // Tasks Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assigned Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        if (taskState.tasks.size > 5) {
                            TextButton(onClick = { /* Navigate to all tasks */ }) {
                                Text("View All", color = Primary)
                            }
                        }
                    }
                }

                // Recent tasks list
                if (taskState.tasks.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No tasks assigned yet",
                            icon = Icons.AutoMirrored.Filled.Assignment
                        )
                    }
                } else {
                    items(taskState.tasks.take(5)) { task ->
                        TaskSmallCard(task = task)
                    }
                }

                // Performance Section Header
                item {
                    Text(
                        text = "Performance Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // Performance Chart
                item {
                    PerformanceTrendCard(
                        reviews = performanceState.reviews,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Recent Reviews
                if (performanceState.reviews.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Reviews",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(performanceState.reviews.take(3)) { review ->
                        PerformanceReviewCard(review = review)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Primary
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
