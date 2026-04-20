package com.mindmatrix.employeetracker.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToLeaderboard: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val employeeState by employeeViewModel.state.collectAsState()
    val taskState by taskViewModel.state.collectAsState()
    val performanceState by performanceViewModel.state.collectAsState()

    val context = LocalContext.current
    val currentEmployee = authState.currentEmployee
    val isAdmin = currentEmployee?.role == UserRole.ADMIN

    var selectedDeptFilter by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>("Overall") }
    var selectedTimeRange by remember { mutableStateOf("Monthly") }

    LaunchedEffect(currentEmployee) {
        if (isAdmin) {
            performanceViewModel.loadDepartmentAverages()
            performanceViewModel.loadLeaderboard()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Reports & Analytics",
                subtitle = if (isAdmin) "Organization-wide performance" else "Team Performance",
                onNotificationClick = { /* Handle notifications */ }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        val csvData = StringBuilder()
                        // Header
                        csvData.append("Employee ID,Employee Name,Department,Average Score,Rank,Category,Date\n")
                        
                        performanceState.leaderboard.forEach { entry ->
                            val employee = employeeState.employees.find { it.name == entry.employeeName }
                            csvData.append("${employee?.id ?: "N/A"},")
                            csvData.append("${entry.employeeName},")
                            csvData.append("${entry.department},")
                            csvData.append("${String.format(Locale.getDefault(), "%.2f", entry.averageScore)},")
                            csvData.append("${entry.rank},")
                            csvData.append("${selectedCategoryFilter ?: "Overall"},")
                            csvData.append("${java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())}\n")
                        }
                        
                        try {
                            val file = File(context.cacheDir, "performance_report_${System.currentTimeMillis()}.csv")
                            file.writeText(csvData.toString())
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export Report"))
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    containerColor = Primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export CSV")
                }
            }
        },
        containerColor = Background
    ) { padding ->
        if (!isAdmin && currentEmployee?.role != UserRole.LEAD) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("You do not have permission to view reports.")
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                // Filters Row
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Department Filter
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDeptFilter == null,
                                onClick = { selectedDeptFilter = null },
                                label = { Text("All Depts") }
                            )
                        }
                        val depts = employeeState.employees.map { it.department }.distinct()
                        items(depts) { dept ->
                            FilterChip(
                                selected = selectedDeptFilter == dept,
                                onClick = { selectedDeptFilter = dept },
                                label = { Text(dept) }
                            )
                        }
                    }

                    // Category Filter
                    val categories = listOf("Overall", "Quality", "Timeliness", "Attendance", "Communication", "Innovation")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryFilter == category,
                                onClick = { selectedCategoryFilter = category },
                                label = { Text(category) },
                                leadingIcon = if (selectedCategoryFilter == category) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // Time Range Filter
                    val timeRanges = listOf("Weekly", "Monthly", "Quarterly", "Yearly")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(timeRanges) { range ->
                            FilterChip(
                                selected = selectedTimeRange == range,
                                onClick = { selectedTimeRange = range },
                                label = { Text(range) }
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Filtered logic for dashboard stats
                    val filteredEmployees = if (selectedDeptFilter == null) {
                        employeeState.employees
                    } else {
                        employeeState.employees.filter { it.department == selectedDeptFilter }
                    }

                    // Overview stats
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            val filteredTasks = if (isAdmin) {
                                if (selectedDeptFilter == null) taskState.tasks
                                else taskState.tasks.filter { task ->
                                    employeeState.employees.any { it.id == task.assignedTo && it.department == selectedDeptFilter }
                                }
                            } else {
                                taskState.tasks.filter { task ->
                                    employeeState.employees.any { it.id == task.assignedTo && it.department == currentEmployee?.department }
                                }
                            }
                            val completedTasks = filteredTasks.count { it.status == TaskStatus.COMPLETED }
                            val completionRate = if (filteredTasks.isNotEmpty())
                                (completedTasks * 100 / filteredTasks.size) else 0

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                AdminStatCard(
                                    label = if (isAdmin) "Total Staff" else "Team Members",
                                    value = "${filteredEmployees.size}",
                                    trend = "+2",
                                    trendColor = Success,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatCard(
                                    label = "Completion",
                                    value = "$completionRate%",
                                    trend = "+5%",
                                    trendColor = Success,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            AdminStatCard(
                                label = "Active Projects",
                                value = "${filteredTasks.size}",
                                subtext = if (isAdmin) {
                                    if (selectedDeptFilter == null) "Across ${performanceState.departmentAverages.size} Depts"
                                    else "In $selectedDeptFilter"
                                } else "In ${currentEmployee?.department}"
                            )
                        }
                    }

                    // Only Admin can see organization-wide department averages and leaderboard
                    if (isAdmin) {
                        // Leaderboard card
                        item {
                            Card(
                                onClick = onNavigateToLeaderboard,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(PrimaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Leaderboard, contentDescription = null, tint = Primary)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Leaderboard",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryDark
                                            )
                                            Text(
                                                text = "View top performers",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Department averages
                        val displayDeptAverages = if (selectedDeptFilter == null) {
                            performanceState.departmentAverages
                        } else {
                            performanceState.departmentAverages.filter { it.department == selectedDeptFilter }
                        }

                        if (displayDeptAverages.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Department Performance",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }

                            items(displayDeptAverages.sortedByDescending { it.averageScore }) { deptAvg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = deptAvg.department,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurface
                                            )
                                            Text(
                                                text = "${deptAvg.employeeCount} employees",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            LinearProgressIndicator(
                                                progress = { (deptAvg.averageScore / 100f).toFloat() },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.85f)
                                                    .height(8.dp)
                                                    .clip(CircleShape),
                                                color = if (deptAvg.averageScore >= 80) Success else if (deptAvg.averageScore >= 60) Primary else Error,
                                                trackColor = Outline.copy(alpha = 0.3f)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.1f", deptAvg.averageScore),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Black,
                                                color = PrimaryDark
                                            )
                                            Text(
                                                text = "AVG SCORE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (currentEmployee?.role == UserRole.LEAD) {
                        // Lead specific views if any, or just limited stats
                        item {
                            Text(
                                text = "Team Insights",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Lead-specific team metrics would go here.")
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}
