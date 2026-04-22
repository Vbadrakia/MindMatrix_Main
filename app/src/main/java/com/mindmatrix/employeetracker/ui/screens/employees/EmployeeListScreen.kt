package com.mindmatrix.employeetracker.ui.screens.employees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel

@Composable
fun EmployeeListScreen(
    onEmployeeClick: (String) -> Unit = {},
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by employeeViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isAdmin = authState.currentEmployee?.role == UserRole.ADMIN

    var deleteTarget by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState.currentEmployee) {
        employeeViewModel.filterByRole(authState.currentEmployee)
    }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { /* add employee flow */ }) {
                    Text("+")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Employees")
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = employeeViewModel::searchEmployees,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search employees") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }
                state.filteredEmployees.isEmpty() -> {
                    Text("No employees found")
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.filteredEmployees) { employee ->
                            Card(colors = CardDefaults.cardColors()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(employee.name)
                                    Text(employee.designation)
                                    Text(employee.department)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        Button(onClick = { onEmployeeClick(employee.id) }) {
                                            Text("View")
                                        }
                                        if (isAdmin) {
                                            IconButton(onClick = { deleteTarget = employee.id }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    employeeViewModel.deleteEmployee(deleteTarget!!)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
            title = { Text("Delete employee") },
            text = { Text("Are you sure you want to delete this employee?") }
        )
    }
}
