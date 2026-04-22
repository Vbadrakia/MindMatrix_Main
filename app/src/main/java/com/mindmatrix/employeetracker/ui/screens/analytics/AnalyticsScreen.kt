package com.mindmatrix.employeetracker.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.R
import com.mindmatrix.employeetracker.data.model.DepartmentAverage
import com.mindmatrix.employeetracker.ui.components.DashboardTopBar
import com.mindmatrix.employeetracker.ui.theme.Background
import com.mindmatrix.employeetracker.ui.theme.Error
import com.mindmatrix.employeetracker.ui.theme.OnSurfaceVariant
import com.mindmatrix.employeetracker.ui.theme.Primary
import com.mindmatrix.employeetracker.ui.theme.PrimaryDark
import com.mindmatrix.employeetracker.ui.theme.Surface
import com.mindmatrix.employeetracker.ui.theme.Success
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlin.math.PI

@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel()
) {
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()

    var selectedDepartment by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    LaunchedEffect(selectedDepartment, startDate, endDate) {
        performanceViewModel.loadAnalytics(
            department = selectedDepartment,
            startDate = startDate.ifBlank { null },
            endDate = endDate.ifBlank { null }
        )
    }

    val analytics = performanceState.analytics

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.analytics),
                subtitle = stringResource(R.string.org_metrics),
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val departments = employeeState.employees.map { it.department }.filter { it.isNotBlank() }.distinct().sorted()
                    AssistChip(
                        onClick = { selectedDepartment = null },
                        label = { Text(stringResource(R.string.all_depts)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedDepartment == null) Primary else Surface,
                            labelColor = if (selectedDepartment == null) Color.White else OnSurfaceVariant
                        )
                    )
                    departments.take(3).forEach { dept ->
                        AssistChip(
                            onClick = { selectedDepartment = dept },
                            label = { Text(dept) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedDepartment == dept) Primary else Surface,
                                labelColor = if (selectedDepartment == dept) Color.White else OnSurfaceVariant
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text(stringResource(R.string.start_date_iso)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text(stringResource(R.string.end_date_iso)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionTitle(stringResource(R.string.avg_rating_per_employee))
                val values = analytics.employeeAverages.map { it.averageRating.toFloat() }.toTypedArray()
                if (values.isNotEmpty()) {
                    ChartCard {
                        Chart(
                            chart = columnChart(),
                            model = entryModelOf(*values),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(220.dp)
                        )
                    }
                } else {
                    EmptyAnalyticsCard()
                }
            }

            item {
                SectionTitle(stringResource(R.string.dept_performance_distribution))
                DepartmentDistributionPieChart(analytics.departmentDistribution)
            }

            item {
                SectionTitle(stringResource(R.string.monthly_perf_trend))
                val trend = analytics.monthlyTrend.map { it.averageRating.toFloat() }.toTypedArray()
                if (trend.isNotEmpty()) {
                    ChartCard {
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(*trend),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(220.dp)
                        )
                    }
                } else {
                    EmptyAnalyticsCard()
                }
            }

            item {
                SectionTitle(stringResource(R.string.top_performers))
                ChartCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        analytics.topPerformers.forEach { entry ->
                            Text("#${entry.rank} ${entry.employeeName} • ${entry.averageScore.toInt()}%")
                        }
                        if (analytics.topPerformers.isEmpty()) {
                            Text(stringResource(R.string.no_data_available), color = OnSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.low_performers))
                ChartCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        analytics.lowPerformers.forEach { entry ->
                            Text("${entry.employeeName} • ${entry.averageScore.toInt()}%")
                        }
                        if (analytics.lowPerformers.isEmpty()) {
                            Text(stringResource(R.string.no_data_available), color = OnSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onNavigateToLeaderboard,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.view_detailed_leaderboard), color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = PrimaryDark
    )
}

@Composable
private fun ChartCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
private fun EmptyAnalyticsCard() {
    ChartCard {
        Text(stringResource(R.string.no_data_available), color = OnSurfaceVariant)
    }
}

@Composable
private fun DepartmentDistributionPieChart(departmentAverages: List<DepartmentAverage>) {
    if (departmentAverages.isEmpty()) {
        EmptyAnalyticsCard()
        return
    }

    val total = departmentAverages.sumOf { it.averageScore }.coerceAtLeast(0.0001)
    val colors = listOf(Primary, Success, Error, Color(0xFF9C27B0), Color(0xFF03A9F4), Color(0xFFFF9800))

    ChartCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(180.dp)) {
                var startAngle = -90f
                departmentAverages.forEachIndexed { index, segment ->
                    val sweep = ((segment.averageScore / total) * 360f).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweep
                }
                drawCircle(Color.White, radius = size.minDimension * 0.28f)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                departmentAverages.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Surface(
                            modifier = Modifier.size(10.dp),
                            shape = CircleShape,
                            color = colors[index % colors.size]
                        ) {}
                        Text("${item.department}: ${item.averageScore.toInt()}%")
                    }
                }
            }
        }
    }
}
