package com.mindmatrix.employeetracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindmatrix.employeetracker.R
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.components.DashboardTopBar
import com.mindmatrix.employeetracker.ui.components.EmployeeAvatar
import com.mindmatrix.employeetracker.ui.theme.*
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import com.mindmatrix.employeetracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isSeeding by settingsViewModel.isSeeding.collectAsStateWithLifecycle()
    val seedSuccess by settingsViewModel.seedSuccess.collectAsStateWithLifecycle()
    val seedError by settingsViewModel.error.collectAsStateWithLifecycle()
    
    val currentEmployee = authState.currentEmployee
    var showSignOutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(seedSuccess, seedError) {
        if (seedSuccess == true) {
            snackbarHostState.showSnackbar(context.getString(R.string.data_seed_success))
            settingsViewModel.clearStatus()
        } else if (seedError != null) {
            snackbarHostState.showSnackbar(context.getString(R.string.error_prefix, seedError))
            settingsViewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.settings),
                subtitle = stringResource(R.string.settings_subtitle)
            )
        },
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = { Text(stringResource(R.string.sign_out), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.sign_out_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        showSignOutDialog = false
                        onSignOut()
                        authViewModel.signOut()
                    }) {
                        Text(stringResource(R.string.sign_out), color = Error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text("Cancel", color = OnSurfaceVariant)
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        EmployeeAvatar(
                            name = currentEmployee?.name ?: stringResource(R.string.user_default),
                            size = 64
                        )
                        Column {
                            Text(
                                text = currentEmployee?.name ?: stringResource(R.string.user_default),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = currentEmployee?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = PrimaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.role_dept_format,
                                        currentEmployee?.role?.name ?: "",
                                        currentEmployee?.department ?: ""
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }

            // Settings sections
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.account_settings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.profile_info),
                        subtitle = stringResource(R.string.profile_info_desc),
                        onClick = { onNavigateToProfile() }
                    )

                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.notifications),
                        subtitle = stringResource(R.string.notifications_settings_desc),
                        onClick = { onNavigateToNotifications() }
                    )
                }
            }

            // Developer options
            if (currentEmployee?.role == UserRole.ADMIN) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.administrative),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            onClick = {
                                if (!isSeeding) {
                                    settingsViewModel.seedData()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(WarningContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = Warning)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.db_maintenance), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (isSeeding) stringResource(R.string.seeding_data) 
                                        else stringResource(R.string.populate_records), 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                                if (isSeeding) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Warning
                                    )
                                } else {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.app_preferences),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.appearance),
                        subtitle = stringResource(R.string.appearance_desc),
                        onClick = { onNavigateToAppearance() }
                    )

                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.about),
                        subtitle = stringResource(R.string.app_version_build),
                        onClick = { onNavigateToAbout() }
                    )
                }
            }

            // Sign out
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorContainer,
                        contentColor = Error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.sign_out_account),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}


@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceVariant
            )
        }
    }
}


