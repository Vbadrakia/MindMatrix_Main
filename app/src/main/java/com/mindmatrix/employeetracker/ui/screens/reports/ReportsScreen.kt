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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.mindmatrix.employeetracker.viewmodel.ReportsViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.utils.CsvReportBuilder
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    reportsViewModel: ReportsViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val currentEmployee = authState.currentEmployee
    val isAdmin = currentEmployee?.role == UserRole.ADMIN
    val isLead = currentEmployee?.role == UserRole.LEAD

    val selectedDeptFilter by reportsViewModel.selectedDeptFilter.collectAsStateWithLifecycle()
    val selectedCategoryFilter by reportsViewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedTimeRange by reportsViewModel.selectedTimeRange.collectAsStateWithLifecycle()

    LaunchedEffect(currentEmployee) {
        if (isAdmin || currentEmployee?.role == UserRole.LEAD) {
            performanceViewModel.loadDepartmentAverages()
            performanceViewModel.loadLeaderboard()
            performanceViewModel.loadAllReviews()
        }
    }

    // Set default department for Lead
    LaunchedEffect(currentEmployee) {
        if (currentEmployee?.role == UserRole.LEAD && selectedDeptFilter == null) {
            reportsViewModel.setSelectedDeptFilter(currentEmployee.department)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.reports_analytics),
                subtitle = if (isAdmin) stringResource(R.string.org_wide_perf) else stringResource(R.string.team_perf),
                onNotificationClick = { /* Handle notifications */ }
            )
        },
        floatingActionButton = {
            if (isAdmin || isLead) {
                FloatingActionButton(
                    onClick = {
                        val leaderboardToExport = reportsViewModel.filterLeaderboard(
                            leaderboard = performanceState.leaderboard,
                            isAdmin = isAdmin,
                            currentDepartment = currentEmployee?.department
                        )
                        val csvData = CsvReportBuilder.buildLeaderboardCsv(
                            entries = leaderboardToExport,
                            employees = employeeState.employees,
                            selectedCategory = selectedCategoryFilter
                        )
                        
                        try {
                            val file = File(context.cacheDir, "performance_report_${System.currentTimeMillis()}.csv")
                            file.writeText(csvData.toString())
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_report)))
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    containerColor = Primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Download, contentDescription = stringResource(R.string.export_csv))
                }
            }
        },
        containerColor = Background
    ) { padding ->
        if (!isAdmin && currentEmployee?.role != UserRole.LEAD) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_permission_reports))
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                // Filters Row
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Department Filter (Only for Admin)
                    if (isAdmin) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedDeptFilter == null,
                                    onClick = { reportsViewModel.setSelectedDeptFilter(null) },
                                    label = { Text(stringResource(R.string.all_depts)) }
                                )
                            }
                            val depts = employeeState.employees.map { it.department }.distinct()
                            items(depts) { dept ->
                                FilterChip(
                                    selected = selectedDeptFilter == dept,
                                    onClick = { reportsViewModel.setSelectedDeptFilter(dept) },
                                    label = { Text(dept) }
                                )
                            }
                        }
                    } else {
                        // For Lead, show their department name as a fixed label
                        Surface(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            color = PrimaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.department_label_format, currentEmployee?.department ?: ""),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }

                    // Category Filter
                    val categories = listOf(
                        stringResource(R.string.filter_overall),
                        stringResource(R.string.filter_quality),
                        stringResource(R.string.filter_timeliness),
                        stringResource(R.string.filter_attendance),
                        stringResource(R.string.filter_communication),
                        stringResource(R.string.filter_innovation)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryFilter == category,
                                onClick = { reportsViewModel.setSelectedCategoryFilter(category) },
                                label = { Text(category) },
                                leadingIcon = if (selectedCategoryFilter == category) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // Time Range Filter
                    val timeRanges = listOf(
                        stringResource(R.string.filter_weekly),
                        stringResource(R.string.filter_monthly),
                        stringResource(R.string.filter_quarterly),
                        stringResource(R.string.filter_yearly)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(timeRanges) { range ->
                            FilterChip(
                                selected = selectedTimeRange == range,
                                onClick = { reportsViewModel.setSelectedTimeRange(range) },
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
                                    label = if (isAdmin) stringResource(R.string.total_staff) else stringResource(R.string.team_members),
                                    value = "${filteredEmployees.size}",
                                    trend = "+2",
                                    trendColor = Success,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatCard(
                                    label = stringResource(R.string.completion),
                                    value = "$completionRate%",
                                    trend = "+5%",
                                    trendColor = Success,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            AdminStatCard(
                                label = stringResource(R.string.active_projects),
                                value = "${filteredTasks.size}",
                                subtext = if (isAdmin) {
                                    if (selectedDeptFilter == null) stringResource(R.string.across_depts, performanceState.departmentAverages.size)
                                    else stringResource(R.string.in_dept, selectedDeptFilter!!)
                                } else stringResource(R.string.in_dept, currentEmployee?.department ?: "")
                            )
                        }
                    }

                    // Monthly Performance Chart
                    item {
                        Text(
                            text = stringResource(R.string.monthly_perf_trend),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        PerformanceTrendCard(
                            reviews = performanceState.reviews,
                            onViewDetailedTrend = onNavigateToAnalytics
                        )
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
                                                text = stringResource(R.string.leaderboard_title),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryDark
                                            )
                                            Text(
                                                text = stringResource(R.string.view_top_performers),
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
                                    text = stringResource(R.string.dept_performance),
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
                                                text = stringResource(R.string.employees_count_format, deptAvg.employeeCount),
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
                                                text = stringResource(R.string.avg_score_label),
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
                        // Team Lead specific insights
                        val teamDept = currentEmployee.department
                        val teamAvg = performanceState.departmentAverages.find { it.department == teamDept }
                        val teamLeaderboard = performanceState.leaderboard.filter { it.department == teamDept }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = stringResource(R.string.team_perf_index),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                                
                                teamAvg?.let { avg ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(24.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.average_score),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = OnSurfaceVariant
                                                )
                                                Text(
                                                    text = String.format(Locale.getDefault(), "%.1f", avg.averageScore),
                                                    style = MaterialTheme.typography.displayMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (avg.averageScore >= 80) Success else Primary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier.size(80.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = { (avg.averageScore / 100f).toFloat() },
                                                    modifier = Modifier.fillMaxSize(),
                                                    strokeWidth = 8.dp,
                                                    color = if (avg.averageScore >= 80) Success else Primary,
                                                    trackColor = Outline.copy(alpha = 0.2f),
                                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                                Text(
                                                    text = "${avg.averageScore.toInt()}%",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                if (teamLeaderboard.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.top_performers_team),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDark,
                                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                                    )
                                    
                                    teamLeaderboard.take(3).forEach { entry ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Surface)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${entry.rank}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (entry.rank == 1) Warning else OnSurfaceVariant,
                                                    modifier = Modifier.width(40.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = entry.employeeName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = stringResource(R.string.perf_score_format, entry.averageScore.toInt()),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = OnSurfaceVariant
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.TrendingUp,
                                                    contentDescription = null,
                                                    tint = Success,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
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
