package com.mindmatrix.employeetracker.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.ui.components.AddTaskDialog

@Composable
fun TaskListScreen(
    taskViewModel: TaskViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    employeeViewModel: EmployeeViewModel = hiltViewModel()
) {
    val state by taskViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    
    val currentUser = authState.currentEmployee
    val canAddTask = currentUser?.role == com.mindmatrix.employeetracker.data.model.UserRole.ADMIN || 
                     currentUser?.role == com.mindmatrix.employeetracker.data.model.UserRole.LEAD

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState.currentEmployee) {
        authState.currentEmployee?.let { employee ->
            if (employee.role == com.mindmatrix.employeetracker.data.model.UserRole.EMPLOYEE) {
                taskViewModel.loadTasksForEmployee(employee.id)
            } else {
                taskViewModel.loadTasks()
            }
            employeeViewModel.filterByRole(employee)
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            employees = employeeState.filteredEmployees,
            onDismiss = { showAddDialog = false },
            onSubmit = { task ->
                taskViewModel.addTask(task)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Tasks",
                subtitle = "${state.filteredTasks.size} assignments",
                onNotificationClick = { /* Handle notifications */ }
            )
        },
        floatingActionButton = {
            if (canAddTask) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { taskViewModel.searchTasks(it) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search tasks...", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant.copy(alpha = 0.6f) 
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null, 
                            tint = Primary 
                        ) 
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { taskViewModel.searchTasks("") }) {
                                Icon(
                                    Icons.Default.Close, 
                                    contentDescription = "Clear", 
                                    tint = OnSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = OnSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Outline,
                        cursorColor = Primary
                    )
                )
            }

            // Status filter
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatusFilterChip(
                        selected = state.selectedStatus == null,
                        onClick = { taskViewModel.filterByStatus(null) },
                        label = "All"
                    )
                }
                items(TaskStatus.entries) { status ->
                    StatusFilterChip(
                        selected = state.selectedStatus == status,
                        onClick = {
                            taskViewModel.filterByStatus(
                                if (state.selectedStatus == status) null else status
                            )
                        },
                        label = status.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }
                    )
                }
            }

            if (state.isLoading) {
                LoadingOverlay(isLoading = true)
            } else if (state.filteredTasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        icon = Icons.Default.AssignmentLate,
                        title = "No tasks found",
                        subtitle = "Check back later for new assignments"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.filteredTasks) { task ->
                        TaskCard(task = task)
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
fun StatusFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ) 
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary,
            selectedLabelColor = Color.White,
            containerColor = Surface,
            labelColor = OnSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Outline,
            selectedBorderColor = Primary
        )
    )
}

@Composable
fun TaskCard(task: com.mindmatrix.employeetracker.data.model.Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        text = task.priority.name.lowercase().replaceFirstChar { it.titlecase() },
                        color = getTaskPriorityColor(task.priority)
                    )
                }
                StatusChip(
                    text = task.status.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
                    color = getTaskStatusColor(task.status)
                )
            }
            
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = Outline.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.dueDate.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Due: ${task.dueDate}",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                IconButton(
                    onClick = { /* Navigate to task detail */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Details",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
