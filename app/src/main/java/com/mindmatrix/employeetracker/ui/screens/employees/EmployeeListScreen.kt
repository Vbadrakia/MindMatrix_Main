package com.mindmatrix.employeetracker.ui.screens.employees

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.components.AddEmployeeDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    onEmployeeClick: (String) -> Unit = {},
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by employeeViewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isAdmin = authState.currentEmployee?.role == UserRole.ADMIN

    var showAddDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState.currentEmployee) {
        employeeViewModel.filterByRole(authState.currentEmployee)
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { employee ->
                employeeViewModel.addEmployee(employee)
                showAddDialog = false
            }
        )
    }

    if (employeeToDelete != null) {
        com.mindmatrix.employeetracker.ui.components.ConfirmationDialog(
            title = "Delete Employee",
            message = "Are you sure you want to delete this employee? This action cannot be undone.",
            onConfirm = {
                employeeViewModel.deleteEmployee(employeeToDelete!!)
                employeeToDelete = null
            },
            onDismiss = { employeeToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Team Directory",
                subtitle = "Organization Personnel"
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Employee")
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
            // Header
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Team Directory",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = PrimaryDark
                )
                Text(
                    text = "Manage and review department personnel.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Search Bar
            TextField(
                value = state.searchQuery,
                onValueChange = { employeeViewModel.searchEmployees(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .heightIn(min = 56.dp),
                placeholder = { Text("Search by name or role...", color = OnSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Primary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark,
                    disabledContainerColor = SurfaceVariantDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Department Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Department:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    modifier = Modifier.padding(end = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 24.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedDepartment == null,
                            onClick = { employeeViewModel.filterByDepartment(null) },
                            label = { Text("All") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Surface,
                                labelColor = OnSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.selectedDepartment == null,
                                borderColor = Outline,
                                selectedBorderColor = Primary
                            )
                        )
                    }
                    val departments = state.employees.map { it.department }.distinct().sorted()
                    items(departments) { dept ->
                        FilterChip(
                            selected = state.selectedDepartment == dept,
                            onClick = { employeeViewModel.filterByDepartment(if (state.selectedDepartment == dept) null else dept) },
                            label = { Text(dept) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Surface,
                                labelColor = OnSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.selectedDepartment == dept,
                                borderColor = Outline,
                                selectedBorderColor = Primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                LoadingOverlay(isLoading = true)
            } else if (state.filteredEmployees.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.PeopleOutline,
                        title = "No teammates found",
                        subtitle = if (state.searchQuery.isNotBlank()) 
                            "We couldn't find anyone matching \"${state.searchQuery}\"." 
                            else "It looks like your team directory is currently empty."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.filteredEmployees) { employee ->
                        EmployeeDirectoryCard(
                            employee = employee,
                            onClick = { onEmployeeClick(employee.id) },
                            onDelete = if (isAdmin) { { employeeToDelete = employee.id } } else null
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun EmployeeDirectoryCard(
    employee: Employee,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Primary)
            )
            
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmployeeAvatar(name = employee.name, size = 60)
                    
                    Surface(
                        color = if (employee.isActive) SuccessContainer else ErrorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (employee.isActive) "ACTIVE" else "ON LEAVE",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (employee.isActive) Success else Error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Text(
                    text = employee.designation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = employee.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainer,
                            contentColor = PrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("View Profile", fontWeight = FontWeight.Bold)
                    }
                    
                    if (onDelete != null) {
                        FilledTonalIconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = com.mindmatrix.employeetracker.ui.theme.ErrorContainer,
                                contentColor = com.mindmatrix.employeetracker.ui.theme.Error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete ${employee.name}"
                            )
                        }
                    }
                }
            }
        }
    }
}
