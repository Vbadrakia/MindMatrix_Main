package com.mindmatrix.employeetracker.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AttendanceViewModel
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.mindmatrix.employeetracker.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val attendanceState by attendanceViewModel.state.collectAsStateWithLifecycle()
    val currentEmployee = authState.currentEmployee

    LaunchedEffect(currentEmployee) {
        currentEmployee?.let {
            attendanceViewModel.loadAttendanceForEmployee(it.id)
            attendanceViewModel.checkTodayAttendance(it.id)
            attendanceViewModel.loadAttendanceSummary(it.id)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Attendance",
                subtitle = "Manage your daily presence"
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
            // Today's status card
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
                        val statusColor = if (attendanceState.isCheckedIn) Success else Primary
                        
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (attendanceState.isCheckedIn) Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = statusColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (attendanceState.isCheckedIn) "You're Checked In" else "Not Checked In",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        
                        if (attendanceState.todayRecord != null) {
                            val record = attendanceState.todayRecord!!
                            Text(
                                text = "Checked in at ${record.checkInTime}" +
                                        if (record.checkOutTime.isNotBlank()) " • Out at ${record.checkOutTime}" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                currentEmployee?.let {
                                    if (attendanceState.isCheckedIn) {
                                        attendanceViewModel.checkOut(it.id)
                                    } else {
                                        attendanceViewModel.checkIn(it.id)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (attendanceState.isCheckedIn) PrimaryContainer else PrimaryDark,
                                contentColor = if (attendanceState.isCheckedIn) PrimaryDark else Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(
                                if (attendanceState.isCheckedIn) Icons.Default.Logout else Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (attendanceState.isCheckedIn) "Check Out Now" else "Check In Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Summary
            if (attendanceState.summary.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Monthly Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            attendanceState.summary.take(3).forEach { statusCount ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${statusCount.count}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = getAttendanceStatusColor(statusCount.status)
                                        )
                                        Text(
                                            text = statusCount.status.name.lowercase(Locale.ROOT)
                                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // History
            item {
                Text(
                    text = "Recent History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }

            if (attendanceState.records.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventBusy,
                        title = "No attendance records",
                        subtitle = "Your attendance history will appear here"
                    )
                }
            } else {
                items(attendanceState.records.sortedByDescending { it.date }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(getAttendanceStatusColor(record.status).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = getAttendanceStatusColor(record.status)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = record.date,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${record.checkInTime} - ${record.checkOutTime.ifBlank { "—" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusChip(
                                    text = record.status.name.replace("_", " "),
                                    color = getAttendanceStatusColor(record.status)
                                )
                                if (record.hoursWorked > 0) {
                                    Text(
                                        text = String.format("%.1fh worked", record.hoursWorked),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

