package com.mindmatrix.employeetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.utils.UiState

@Composable
fun DashboardScreen(viewModel: EmployeeViewModel = hiltViewModel()) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEmployees()
    }

    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Employees loaded successfully")
            }
        }

        is UiState.Error -> {
            Text("Error loading dashboard")
        }
    }
}
