package com.mindmatrix.employeetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.utils.UiState

@Composable
fun EmployeeScreen(viewModel: EmployeeViewModel = hiltViewModel()) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEmployees()
    }

    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val employees = (state as UiState.Success<*>).data as List<*>
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(employees) { item ->
                    Text(text = item.toString(), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        is UiState.Error -> {
            Text("Error loading employees")
        }
    }
}
