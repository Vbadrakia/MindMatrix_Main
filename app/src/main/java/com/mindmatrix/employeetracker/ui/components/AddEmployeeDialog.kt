package com.mindmatrix.employeetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindmatrix.employeetracker.R
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.theme.Primary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeDialog(
    employeeToEdit: Employee? = null,
    availableDepartments: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (Employee) -> Unit
) {
    val isEditMode = employeeToEdit != null
    var name by remember { mutableStateOf(employeeToEdit?.name ?: "") }
    var email by remember { mutableStateOf(employeeToEdit?.email ?: "") }
    var designation by remember { mutableStateOf(employeeToEdit?.designation ?: "") }
    var department by remember { mutableStateOf(employeeToEdit?.department ?: "") }
    var role by remember { mutableStateOf(employeeToEdit?.role ?: UserRole.EMPLOYEE) }
    var phone by remember { mutableStateOf(employeeToEdit?.phone ?: "") }
    
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) stringResource(R.string.edit_employee) else stringResource(R.string.add_employee),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email_address)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text(stringResource(R.string.designation)) },
                    modifier = Modifier.fillMaxWidth()
                )
                var departmentExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = departmentExpanded,
                    onExpandedChange = { departmentExpanded = !departmentExpanded }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text(stringResource(R.string.department)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    if (availableDepartments.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = departmentExpanded,
                            onDismissRequest = { departmentExpanded = false }
                        ) {
                            availableDepartments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        department = dept
                                        departmentExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_number)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.role)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        UserRole.entries.forEach { userRole ->
                            DropdownMenuItem(
                                text = { Text(userRole.name) },
                                onClick = {
                                    role = userRole
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        val newEmployee = Employee(
                            id = employeeToEdit?.id ?: "",
                            name = name,
                            email = email,
                            designation = designation,
                            department = department,
                            role = role,
                            phone = phone,
                            joinDate = employeeToEdit?.joinDate ?: java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                            isActive = employeeToEdit?.isActive ?: true,
                            profileImageUrl = employeeToEdit?.profileImageUrl ?: "",
                            managerId = employeeToEdit?.managerId ?: ""
                        )
                        onSubmit(newEmployee)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.add_employee))
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
