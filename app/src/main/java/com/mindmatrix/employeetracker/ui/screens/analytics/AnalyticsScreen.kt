package com.mindmatrix.employeetracker.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.ui.components.DashboardTopBar
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.util.Locale

@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    performanceViewModel: PerformanceViewModel = hiltViewModel()
) {
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        performanceViewModel.loadDepartmentAverages()
        performanceViewModel.loadLeaderboard()
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Analytics",
                subtitle = "Organization wide metrics",
                onBackClick = onNavigateBack,
                onNotificationClick = null
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
            item {
                Text(
                    text = "Department Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                DepartmentPerformanceChart(performanceState.departmentAverages)
            }

            item {
                Text(
                    text = "Task Status Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                TaskDistributionChart()
            }

            item {
                Text(
                    text = "Company Growth Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                CompanyTrendChart()
            }
            
            item {
                Button(
                    onClick = onNavigateToLeaderboard,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Detailed Leaderboard", color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun TaskDistributionChart() {
    val chartEntryModel = entryModelOf(45f, 25f, 20f, 10f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Overall Task Completion Distribution",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = columnChart(),
                model = chartEntryModel,
                modifier = Modifier.height(220.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Legend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                LegendItem("Done", Success)
                LegendItem("Ongoing", Tertiary)
                LegendItem("Pending", Primary)
                LegendItem("Overdue", Error)
            }
        }
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = color) {}
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun DepartmentPerformanceChart(departmentAverages: List<com.mindmatrix.employeetracker.data.model.DepartmentAverage>) {
    // Fallback data if empty
    val deptData = departmentAverages.ifEmpty {
        listOf(
            com.mindmatrix.employeetracker.data.model.DepartmentAverage("Engineering", 4.2),
            com.mindmatrix.employeetracker.data.model.DepartmentAverage("Design", 4.5),
            com.mindmatrix.employeetracker.data.model.DepartmentAverage("Sales", 3.8),
            com.mindmatrix.employeetracker.data.model.DepartmentAverage("Marketing", 4.6)
        )
    }

    val values = deptData.map { it.averageScore.toFloat() }.toTypedArray()
    val chartEntryModel = entryModelOf(*values)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Average Score per Department",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = columnChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(220.dp)
            )
        }
    }
}

@Composable
fun CompanyTrendChart() {
    // Mock trend data
    val chartEntryModel = entryModelOf(3.2f, 3.5f, 3.8f, 4.0f, 4.2f, 4.5f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Overall Performance Trend (Last 6 Months)",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(220.dp)
            )
        }
    }
}
