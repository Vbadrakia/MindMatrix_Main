package com.mindmatrix.employeetracker.ui.screens.dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDashboardScreen(
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val currentEmployee = authState.currentEmployee

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Lead Dashboard",
                subtitle = "Team Overview & Analytics",
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
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Pending Tasks",
                        value = "${taskState.tasks.count { it.status.name == "PENDING" }}",
                        trend = "High",
                        trendColor = StatusLate,
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
                LeadTaskCard(task)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LeadTaskCard(task: com.mindmatrix.employeetracker.data.model.Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        text = "Priority: ${task.priority.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                StatusChip(
                    text = task.status.name.replace("_", " "),
                    color = getTaskStatusColor(task.status)
                )
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
                    Text(text = "Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
        }
    }
}
