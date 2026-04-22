package com.mindmatrix.employeetracker.ui.screens.departments

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.data.model.Department
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.DepartmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DepartmentViewModel = hiltViewModel(),
    employeeViewModel: com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val employeeState by employeeViewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var departmentToEdit by remember { mutableStateOf<Department?>(null) }
    var departmentToDelete by remember { mutableStateOf<Department?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.departments_title),
                subtitle = stringResource(R.string.departments_subtitle),
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_department_desc))
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingOverlay(isLoading = true)
            } else if (state.departments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Business,
                        title = stringResource(R.string.no_departments_found),
                        subtitle = stringResource(R.string.no_departments_subtitle)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.departments) { department ->
                        DepartmentCard(
                            department = department,
                            onEdit = { departmentToEdit = department },
                            onDelete = { departmentToDelete = department }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog || departmentToEdit != null) {
        AddEditDepartmentDialog(
            department = departmentToEdit,
            employees = employeeState.employees,
            onDismiss = {
                showAddDialog = false
                departmentToEdit = null
            },
            onSubmit = { name, desc, headId ->
                if (departmentToEdit != null) {
                    viewModel.updateDepartment(departmentToEdit!!.copy(name = name, description = desc, headId = headId))
                } else {
                    viewModel.addDepartment(name, desc, headId)
                }
                showAddDialog = false
                departmentToEdit = null
            }
        )
    }

    if (departmentToDelete != null) {
        ConfirmationDialog(
            title = stringResource(R.string.delete_department_title),
            message = stringResource(
                R.string.delete_department_message,
                departmentToDelete?.name ?: ""
            ),
            onConfirm = {
                viewModel.deleteDepartment(departmentToDelete!!.id)
                departmentToDelete = null
            },
            onDismiss = { departmentToDelete = null }
        )
    }
}

@Composable
fun DepartmentCard(
    department: Department,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = department.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    if (department.description.isNotBlank()) {
                        Text(
                            text = department.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (department.headId.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.department_head_format, department.headId),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = Primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDepartmentDialog(
    department: Department? = null,
    employees: List<com.mindmatrix.employeetracker.data.model.Employee> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(department?.name ?: "") }
    var description by remember { mutableStateOf(department?.description ?: "") }
    var headId by remember { mutableStateOf(department?.headId ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (department != null) {
                    stringResource(R.string.edit_department_title)
                } else {
                    stringResource(R.string.add_department_title)
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.department_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = headId,
                        onValueChange = { headId = it },
                        label = { Text(stringResource(R.string.hod_label)) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.none)) },
                            onClick = {
                                headId = ""
                                expanded = false
                            }
                        )
                        employees.filter { it.role == com.mindmatrix.employeetracker.data.model.UserRole.LEAD || it.role == com.mindmatrix.employeetracker.data.model.UserRole.ADMIN }.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text("${employee.name} (${employee.role})") },
                                onClick = {
                                    headId = employee.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSubmit(name, description, headId) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (department != null) stringResource(R.string.save) else stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
