package com.mindmatrix.employeetracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.mindmatrix.employeetracker.viewmodel.InsightsViewModel
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import java.util.Locale
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    insightsViewModel: InsightsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.currentEmployee) {
        authState.currentEmployee?.let { user ->
            insightsViewModel.generateInsights(UserRole.ADMIN, user.id)
            performanceViewModel.loadDepartmentAverages()
        }
        performanceViewModel.loadLeaderboard()
    }

    val isRefreshing by taskViewModel.isRefreshing.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            authState.currentEmployee?.let { user ->
                insightsViewModel.generateInsights(UserRole.ADMIN, user.id)
                performanceViewModel.loadDepartmentAverages()
                taskViewModel.loadTasks()
                employeeViewModel.searchEmployees("") // reload all
            }
            performanceViewModel.loadLeaderboard()
        }
    )

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Admin Dashboard",
                subtitle = "Organization Overview",
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
            // Insights Engine Section
            item {
                InsightsCard(insights = insightsState.insights)
            }

            // Stat Cards Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        label = "Employees",
                        value = "${employeeState.employees.size}",
                        trend = "+4%",
                        trendColor = StatusPresent,
                        onClick = onNavigateToEmployees,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Active Tasks",
                        value = "${taskState.tasks.count { it.status != com.mindmatrix.employeetracker.data.model.TaskStatus.COMPLETED && it.status != com.mindmatrix.employeetracker.data.model.TaskStatus.REVIEWED }}",
                        subtext = "${employeeState.employees.map { it.department }.distinct().size} teams",
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                AdminStatCard(
                    label = "Average Performance",
                    value = String.format(Locale.getDefault(), "%.1f / 5.0", performanceState.averageScore),
                    isRating = true,
                    onClick = onNavigateToReports,
                    modifier = Modifier.fillMaxWidth(),
                    rating = performanceState.averageScore.toInt()
                )
            }

            // Top Performers Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Performers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        TextButton(onClick = onNavigateToEmployees) {
                            Text("View All", color = Primary)
                        }
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 24.dp)
                    ) {
                        items(employeeState.employees.take(5)) { employee ->
                            TopPerformerCard(employee)
                        }
                    }
                }
            }

            // Performance Trend Chart Section
            item {
                PerformanceTrendCard(
                    reviews = performanceState.reviews,
                    onViewDetailedTrend = onNavigateToAnalytics
                )
            }

            // Workload Distribution Section
            item {
                WorkloadDistributionCard(
                    employees = employeeState.employees,
                    tasks = taskState.tasks
                )
            }

            // Department Scores Section
            item {
                DepartmentScoresCard(
                    departmentAverages = performanceState.departmentAverages,
                    onNavigateToReports = onNavigateToReports
                )
            }
            
            // Extra spacing for bottom navigation
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
}
