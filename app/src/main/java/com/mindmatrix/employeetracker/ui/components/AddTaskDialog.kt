package com.mindmatrix.employeetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindmatrix.employeetracker.R
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskPriority
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.ui.theme.Primary
import com.mindmatrix.employeetracker.ui.screens.dashboard.getTaskPriorityStringRes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onSubmit: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var assignedToId by remember { mutableStateOf(employees.firstOrNull()?.id ?: "") }
    
    var priorityExpanded by remember { mutableStateOf(false) }
    var employeeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (dueDate.isNotBlank()) {
                try {
                    LocalDate.parse(dueDate, dateFormatter)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                } catch (e: Exception) {
                    Instant.now().toEpochMilli()
                }
            } else {
                Instant.now().toEpochMilli()
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dueDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(dateFormatter)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.assign_task),
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.due_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.select_date))
                        }
                    }
                )
                
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        value = stringResource(getTaskPriorityStringRes(priority)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.priority)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        TaskPriority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(stringResource(getTaskPriorityStringRes(p))) },
                                onClick = {
                                    priority = p
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = employeeExpanded,
                    onExpandedChange = { employeeExpanded = !employeeExpanded }
                ) {
                    val selectedEmployeeName = employees.find { it.id == assignedToId }?.name ?: stringResource(R.string.select_employee)
                    OutlinedTextField(
                        value = selectedEmployeeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.assign_to)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = employeeExpanded,
                        onDismissRequest = { employeeExpanded = false }
                    ) {
                        employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee.name) },
                                onClick = {
                                    assignedToId = employee.id
                                    employeeExpanded = false
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
                    if (title.isNotBlank() && assignedToId.isNotBlank()) {
                        val newTask = Task(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            assignedTo = assignedToId,
                            status = TaskStatus.PENDING,
                            priority = priority,
                            dueDate = dueDate
                        )
                        onSubmit(newTask)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.create_task), fontWeight = FontWeight.Bold)
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
