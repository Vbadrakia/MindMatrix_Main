package com.mindmatrix.employeetracker.ui.screens.employees

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindmatrix.employeetracker.ui.components.*
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.EmployeeViewModel
import com.mindmatrix.employeetracker.viewmodel.PerformanceViewModel
import com.mindmatrix.employeetracker.viewmodel.TaskViewModel
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.mindmatrix.employeetracker.ui.components.AddPerformanceReviewDialog
import com.mindmatrix.employeetracker.ui.screens.performance.PerformanceReviewCard
import com.mindmatrix.employeetracker.data.model.TaskStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmployeeDetailScreen(
    employeeId: String,
    onNavigateBack: () -> Unit = {},
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    departmentViewModel: com.mindmatrix.employeetracker.viewmodel.DepartmentViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    performanceViewModel: PerformanceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    documentViewModel: com.mindmatrix.employeetracker.viewmodel.DocumentViewModel = hiltViewModel(),
    notificationViewModel: com.mindmatrix.employeetracker.viewmodel.NotificationViewModel = hiltViewModel()
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    
    val selectedEmployee by employeeViewModel.selectedEmployee.collectAsState()
    val departmentState by departmentViewModel.state.collectAsState()
    val taskState by taskViewModel.state.collectAsState()
    val performanceState by performanceViewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val documentState by documentViewModel.state.collectAsState()

    val currentUser = authState.currentEmployee
    val isAdmin = currentUser?.role == com.mindmatrix.employeetracker.data.model.UserRole.ADMIN
    val isLead = currentUser?.role == com.mindmatrix.employeetracker.data.model.UserRole.LEAD
    val isOwnProfile = currentUser?.id == employeeId

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            documentViewModel.uploadDocument(employeeId, it, "New Document", "PDF")
        }
    }

    LaunchedEffect(employeeId) {
        employeeViewModel.loadEmployeeById(employeeId)
        taskViewModel.loadTasksForEmployee(employeeId)
        performanceViewModel.loadReviewsForEmployee(employeeId)
        performanceViewModel.loadAverageScore(employeeId)
        documentViewModel.loadDocuments(employeeId)
    }

    val employee = selectedEmployee
    var showReviewDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<com.mindmatrix.employeetracker.data.model.PerformanceReview?>(null) }
    var reviewToDelete by remember { mutableStateOf<com.mindmatrix.employeetracker.data.model.PerformanceReview?>(null) }

    if (showEditDialog && employee != null) {
        com.mindmatrix.employeetracker.ui.components.AddEmployeeDialog(
            employeeToEdit = employee,
            availableDepartments = departmentState.departments.map { it.name }.distinct().sorted(),
            onDismiss = { showEditDialog = false },
            onSubmit = { updatedEmployee ->
                employeeViewModel.updateEmployee(updatedEmployee)
                showEditDialog = false
            }
        )
    }

    if (showReviewDialog && employee != null) {
        AddPerformanceReviewDialog(
            employeeName = employee.name,
            onDismiss = { showReviewDialog = false },
            onSubmit = { quality, timeliness, attendance, communication, innovation, comments, period, _ ->
                val review = com.mindmatrix.employeetracker.data.model.PerformanceReview(
                    employeeId = employee.id,
                    reviewerId = currentUser?.id ?: "",
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                    period = period,
                    qualityScore = quality,
                    timelinessScore = timeliness,
                    attendanceScore = attendance,
                    communicationScore = communication,
                    innovationScore = innovation,
                    comments = comments,
                    remarks = comments
                ).withCalculatedScores()
                performanceViewModel.addReview(review)
                showReviewDialog = false
            }
        )
    }

    if (reviewToEdit != null && employee != null) {
        AddPerformanceReviewDialog(
            employeeName = employee.name,
            onDismiss = { reviewToEdit = null },
            onSubmit = { quality, timeliness, attendance, communication, innovation, comments, period, _ ->
                val existing = reviewToEdit ?: return@AddPerformanceReviewDialog
                val review = existing.copy(
                    period = period,
                    qualityScore = quality,
                    timelinessScore = timeliness,
                    attendanceScore = attendance,
                    communicationScore = communication,
                    innovationScore = innovation,
                    comments = comments,
                    remarks = comments
                ).withCalculatedScores()
                performanceViewModel.updateReview(review)
                reviewToEdit = null
            }
        )
    }

    val employeeState by employeeViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(employeeState.error) {
        employeeState.error?.let {
            snackbarHostState.showSnackbar(it)
            employeeViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.employee_profile),
                onBackClick = onNavigateBack,
                actions = {
                    if (isAdmin) {
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_employee_profile))
                        }
                    }
                }
            )
        },
        containerColor = Background
    ) { padding ->
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            employeeViewModel.uploadProfileImage(employeeId, it)
        }
    }

    if (employee == null) {
        LoadingOverlay(isLoading = true, modifier = Modifier.padding(padding))
    } else {
        // Check access: Leads can only view employees in their own department
        val hasAccess = isAdmin || (isLead && employee.department == currentUser?.department) || isOwnProfile
        
        if (!hasAccess) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_permission_profile))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            EmployeeAvatar(
                                name = employee.name,
                                photoUrl = employee.profileImageUrl,
                                size = 80,
                                onClick = if (isOwnProfile || isAdmin) {
                                    { imagePickerLauncher.launch("image/*") }
                                } else null
                            )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = employee.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                                Text(
                                    text = employee.designation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OnSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(
                                        text = employee.department,
                                        color = Primary
                                    )
                                    StatusChip(
                                        text = employee.role.name.lowercase().replaceFirstChar { it.titlecase() },
                                        color = SecondaryDark
                                    )
                                }

                                // Badges Section
                                val allBadges = mutableListOf<Pair<String, Color>>()
                                if (performanceState.averageScore >= 90) {
                                    allBadges.add(stringResource(R.string.top_performer) to Color(0xFFFFD700)) // Gold
                                }
                                if (taskState.tasks.count { it.status == TaskStatus.COMPLETED } >= 5) {
                                    allBadges.add(stringResource(R.string.goal_crusher) to Color(0xFF4CAF50)) // Green
                                }
                                employee.badges.forEach { badge ->
                                    allBadges.add(badge to Secondary)
                                }

                                if (allBadges.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        allBadges.forEach { (text, color) ->
                                            BadgeChip(
                                                text = text, 
                                                color = color,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }

                                if (isLead && !isOwnProfile && employee.department == currentUser?.department) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { showReviewDialog = true },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.RateReview, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(stringResource(R.string.review))
                                        }
                                        OutlinedButton(
                                            onClick = { showFeedbackDialog = true },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Message, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(stringResource(R.string.feedback))
                                        }
                                    }
                                }
                            }
                        }
                    }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.assigned_tasks_header),
                            value = "${taskState.tasks.size}",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            gradientColors = listOf(Primary, PrimaryLight),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.performance),
                            value = String.format(Locale.getDefault(), "%.1f / 100", performanceState.averageScore),
                            icon = Icons.Default.Assessment,
                            gradientColors = listOf(SecondaryDark, Secondary),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Information Section Header
                item {
                    Text(
                        text = stringResource(R.string.contact_information),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // Contact info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Default.Email,
                                label = stringResource(R.string.email_address),
                                value = employee.email
                            )
                            if (employee.phone.isNotBlank()) {
                                InfoRow(
                                    icon = Icons.Default.Phone,
                                    label = stringResource(R.string.phone_number),
                                    value = employee.phone
                                )
                            }
                            if (employee.joinDate.isNotBlank()) {
                                InfoRow(
                                    icon = Icons.Default.CalendarToday,
                                    label = stringResource(R.string.joining_date),
                                    value = employee.joinDate
                                )
                            }
                        }
                    }
                }

                // Documents Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.documents_attachments),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        if (isOwnProfile || isAdmin) {
                            TextButton(onClick = { documentPickerLauncher.launch("*/*") }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.upload))
                            }
                        }
                    }
                }

                if (documentState.documents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(R.string.no_documents), style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    items(documentState.documents) { doc ->
                        DocumentItem(
                            document = doc,
                            onDelete = if (isOwnProfile || isAdmin) { { documentViewModel.deleteDocument(doc) } } else null
                        )
                    }
                }

                // Tasks Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.assigned_tasks_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        if (taskState.tasks.size > 5) {
                            TextButton(onClick = { /* Navigate to all tasks */ }) {
                                Text(stringResource(R.string.view_all), color = Primary)
                            }
                        }
                    }
                }

                // Recent tasks list
                if (taskState.tasks.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = stringResource(R.string.no_tasks_assigned),
                            icon = Icons.AutoMirrored.Filled.Assignment
                        )
                    }
                } else {
                    items(taskState.tasks.take(5)) { task ->
                        TaskSmallCard(task = task)
                    }
                }

                // Performance Section Header
                item {
                    Text(
                        text = stringResource(R.string.performance_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // Performance Chart
                item {
                    PerformanceTrendCard(
                        reviews = performanceState.reviews,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Recent Reviews
                if (performanceState.reviews.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.recent_reviews),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(performanceState.reviews.take(3)) { review ->
                        val canApprove = (isAdmin || (isLead && currentUser?.department == selectedEmployee?.department)) && !isOwnProfile
                        val canManageReview = isAdmin || (isLead && currentUser?.department == selectedEmployee?.department)
                        PerformanceReviewCard(
                            review = review,
                            showApproveButton = canApprove,
                            onApprove = { performanceViewModel.approveReview(review.id) },
                            onEdit = if (canManageReview) ({ reviewToEdit = review }) else null,
                            onDelete = if (canManageReview) ({ reviewToDelete = review }) else null
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { 
                showFeedbackDialog = false 
                feedbackText = ""
            },
            title = { Text(stringResource(R.string.send_feedback)) },
            text = {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text(stringResource(R.string.message)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedEmployee?.let { emp ->
                            notificationViewModel.sendNotification(
                                com.mindmatrix.employeetracker.data.model.Notification(
                                    recipientId = emp.id,
                                    senderId = currentUser?.id ?: "",
                                    senderName = currentUser?.name ?: "",
                                    title = String.format(Locale.getDefault(), stringResource(R.string.feedback_from), currentUser?.name ?: ""),
                                    message = feedbackText,
                                    type = com.mindmatrix.employeetracker.data.model.NotificationType.GENERAL,
                                    timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                                )
                            )
                        }
                        showFeedbackDialog = false
                        feedbackText = ""
                    },
                    enabled = feedbackText.isNotBlank()
                ) {
                    Text(stringResource(R.string.send))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showFeedbackDialog = false
                    feedbackText = ""
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    reviewToDelete?.id?.let(performanceViewModel::deleteReview)
                    reviewToDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.confirm_delete_review)) }
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Primary
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DocumentItem(
    document: com.mindmatrix.employeetracker.data.model.Document,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (document.type == "PDF") Icons.Default.PictureAsPdf else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${document.type} • ${document.uploadDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Error)
                }
            }
        }
    }
}
