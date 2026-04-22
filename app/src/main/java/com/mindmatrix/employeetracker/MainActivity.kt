package com.mindmatrix.employeetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindmatrix.employeetracker.ui.navigation.AppNavGraph
import com.mindmatrix.employeetracker.ui.navigation.NavigationItems
import com.mindmatrix.employeetracker.ui.navigation.Screen
import com.mindmatrix.employeetracker.ui.theme.EmployeeTrackerTheme
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmployeeTrackerTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: com.mindmatrix.employeetracker.viewmodel.NotificationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val notificationState by notificationViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(authState.currentEmployee) {
        authState.currentEmployee?.let {
            notificationViewModel.loadNotifications(it.id)
        }
    }

    LaunchedEffect(notificationState.notifications) {
        val unread = notificationState.notifications.filter { !it.isRead }
        if (unread.isNotEmpty()) {
            val latest = unread.first()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "New Message: ${latest.title}",
                    actionLabel = "View"
                )
            }
            notificationViewModel.markAsRead(latest.id)
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    if (authState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navItems = when (authState.currentEmployee?.role) {
        com.mindmatrix.employeetracker.data.model.UserRole.ADMIN -> NavigationItems.adminItems
        com.mindmatrix.employeetracker.data.model.UserRole.LEAD -> NavigationItems.leadItems
        else -> NavigationItems.employeeItems
    }

    val showBottomBar = authState.isLoggedIn && currentRoute !in listOf(
        Screen.Login.route,
        Screen.EmployeeDetail.route,
        Screen.Leaderboard.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(it, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavGraph(navController, authViewModel)
        }
    }
}
