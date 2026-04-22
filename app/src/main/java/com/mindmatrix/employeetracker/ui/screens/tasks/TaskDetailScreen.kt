package com.mindmatrix.employeetracker.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.data.model.Task
import com.mindmatrix.employeetracker.data.model.TaskStatus
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.components.StatusChip
import com.mindmatrix.employeetracker.ui.components.getTaskPriorityColor
import com.mindmatrix.employeetracker.ui.components.getTaskStatusColor
import com.mindmatrix.employeetracker.ui.components.getLocalizedTaskStatus
import com.mindmatrix.employeetracker.ui.components.getLocalizedTaskPriority
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by taskViewModel.state.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val task = state.tasks.find { it.id == taskId }
    val currentUser = authState.currentEmployee

    var comments by remember(task) { mutableStateOf(task?.comments ?: "") }
    var isEditingComments by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = PrimaryDark,
                    navigationIconContentColor = PrimaryDark
                )
            )
        },
        containerColor = Background
    ) { padding ->
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Task Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusChip(
                                text = getLocalizedTaskStatus(task.status),
                                color = getTaskStatusColor(task.status)
                            )
                            StatusChip(
                                text = getLocalizedTaskPriority(task.priority),
                                color = getTaskPriorityColor(task.priority)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.due_date_format, task.dueDate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Text(
                            text = task.description.ifBlank { stringResource(R.string.no_description) },
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                    }
                }

                // Comments Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.comments_notes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        if (!isEditingComments) {
                            IconButton(onClick = { isEditingComments = true }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_comments_desc), tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    if (isEditingComments) {
                        OutlinedTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.add_comments_hint)) },
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { 
                                comments = task.comments
                                isEditingComments = false 
                            }) {
                                Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    taskViewModel.updateTask(task.copy(comments = comments))
                                    isEditingComments = false
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Text(
                                text = task.comments.ifBlank { stringResource(R.string.no_comments) },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }
                    }
                }

                // Attachments (Simulated)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.attachments),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (task.attachments.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_attachments),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                            } else {
                                task.attachments.forEach { attachment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = attachment, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { /* Simulate adding attachment */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.add_attachment))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons based on Role and Status
                if (currentUser?.role == UserRole.EMPLOYEE && task.status != TaskStatus.COMPLETED && task.status != TaskStatus.REVIEWED) {
                    Button(
                        onClick = { taskViewModel.updateTaskStatus(task.id, TaskStatus.COMPLETED) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.mark_as_completed), fontWeight = FontWeight.Bold)
                    }
                }

                if ((currentUser?.role == UserRole.LEAD || currentUser?.role == UserRole.ADMIN) && task.status == TaskStatus.COMPLETED) {
                    Button(
                        onClick = { taskViewModel.updateTaskStatus(task.id, TaskStatus.REVIEWED) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.approve_and_review), fontWeight = FontWeight.Bold)
                    }
                }
                
                if (task.status == TaskStatus.PENDING && currentUser?.id == task.assignedTo) {
                    Button(
                        onClick = { taskViewModel.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Tertiary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.start_task), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
