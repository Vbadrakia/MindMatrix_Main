package com.mindmatrix.employeetracker.ui.screens.dashboard

import androidx.compose.ui.res.stringResource
import com.mindmatrix.employeetracker.R
import com.mindmatrix.employeetracker.viewmodel.DepartmentViewModel
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
    departmentViewModel: com.mindmatrix.employeetracker.viewmodel.DepartmentViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDepartments: () -> Unit = {}
) {
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
    val departmentState by departmentViewModel.state.collectAsStateWithLifecycle()
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
                title = stringResource(R.string.admin_dashboard),
                subtitle = stringResource(R.string.org_overview),
                onNotificationClick = onNavigateToNotifications
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

            // Management Section
            item {
                Column {
                    Text(
                        text = stringResource(R.string.org_management),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AdminStatCard(
                            label = stringResource(R.string.employees),
                            value = employeeState.employees.size.toString(),
                            trend = stringResource(R.string.manage_all),
                            trendColor = Primary,
                            onClick = onNavigateToEmployees,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = stringResource(R.string.departments),
                            value = departmentState.departments.size.toString(),
                            trend = stringResource(R.string.manage_all),
                            trendColor = SecondaryDark,
                            onClick = onNavigateToDepartments,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        label = stringResource(R.string.active_tasks),
                        value = taskState.tasks.count { it.status != com.mindmatrix.employeetracker.data.model.TaskStatus.COMPLETED && it.status != com.mindmatrix.employeetracker.data.model.TaskStatus.REVIEWED }.toString(),
                        subtext = stringResource(R.string.across_all_teams),
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = stringResource(R.string.avg_performance),
                        value = String.format(Locale.getDefault(), "%.1f", performanceState.averageScore),
                        isRating = false,
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    )
                }
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
                            text = stringResource(R.string.top_performers),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        TextButton(onClick = onNavigateToEmployees) {
                            Text(stringResource(R.string.view_all), color = Primary)
                        }
                    }
                    val leaderboard = performanceState.leaderboard.take(5)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 24.dp)
                    ) {
                        if (leaderboard.isNotEmpty()) {
                            items(leaderboard) { entry ->
                                TopPerformerCard(
                                    name = entry.employeeName,
                                    designation = entry.department,
                                    score = entry.averageScore
                                )
                            }
                        } else {
                            items(employeeState.employees.take(5)) { employee ->
                                TopPerformerCard(
                                    name = employee.name,
                                    designation = employee.designation,
                                    score = 0.0
                                )
                            }
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
