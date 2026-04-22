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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    departmentViewModel: com.mindmatrix.employeetracker.viewmodel.DepartmentViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by employeeViewModel.state.collectAsStateWithLifecycle()
    val departmentState by departmentViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isAdmin = authState.currentEmployee?.role == UserRole.ADMIN

    var showAddDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState.currentEmployee) {
        employeeViewModel.filterByRole(authState.currentEmployee)
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            availableDepartments = departmentState.departments.map { it.name }.distinct().sorted(),
            onDismiss = { showAddDialog = false },
            onSubmit = { employee ->
                employeeViewModel.addEmployee(employee)
                showAddDialog = false
            }
        )
    }

    if (employeeToDelete != null) {
        com.mindmatrix.employeetracker.ui.components.ConfirmationDialog(
            title = stringResource(R.string.delete_employee),
            message = stringResource(R.string.delete_employee_confirm),
            onConfirm = {
                employeeViewModel.deleteEmployee(employeeToDelete!!)
                employeeToDelete = null
            },
            onDismiss = { employeeToDelete = null }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            employeeViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.team_directory),
                subtitle = stringResource(R.string.org_personnel)
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_employee_desc))
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
                    text = stringResource(R.string.team_directory),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = PrimaryDark
                )
                Text(
                    text = stringResource(R.string.manage_review_personnel),
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
                placeholder = { Text(stringResource(R.string.search_hint), color = OnSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_icon_desc), tint = Primary) },
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
            
            if (isAdmin) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.department_label),
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
                                label = { Text(stringResource(R.string.all)) },
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
                        val departments = departmentState.departments.map { it.name }.distinct().sorted()
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
                        title = stringResource(R.string.no_teammates_found),
                        subtitle = if (state.searchQuery.isNotBlank()) 
                            stringResource(R.string.no_teammates_matching, state.searchQuery) 
                            else stringResource(R.string.team_directory_empty)
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
                            text = if (employee.isActive) stringResource(R.string.active) else stringResource(R.string.on_leave),
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
                        Text(stringResource(R.string.view_profile), fontWeight = FontWeight.Bold)
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
                                contentDescription = stringResource(R.string.delete_target, employee.name)
                            )
                        }
                    }
                }
            }
        }
    }
}
