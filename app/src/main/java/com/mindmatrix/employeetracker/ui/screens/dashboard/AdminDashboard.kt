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
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        performanceViewModel.loadLeaderboard()
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Admin Dashboard",
                subtitle = "Organization Overview",
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
            // Stat Cards Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCard(
                        label = "Total Employees",
                        value = "${employeeState.employees.size}",
                        trend = "+4%",
                        trendColor = StatusPresent
                    )
                    AdminStatCard(
                        label = "Active Tasks",
                        value = "${taskState.tasks.count { it.status.name != "COMPLETED" }}",
                        subtext = "Across ${employeeState.employees.map { it.department }.distinct().size} teams"
                    )
                    AdminStatCard(
                        label = "Average Rating",
                        value = String.format(Locale.getDefault(), "%.1f", performanceState.averageScore),
                        isRating = true
                    )
                }
            }

            // Top Performers Section
            item {
                Column {
                    Text(
                        text = "Top Performers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
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
                PerformanceTrendCard()
            }

            // Department Scores Section
            item {
                DepartmentScoresCard()
            }
            
            // Extra spacing for bottom navigation
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
