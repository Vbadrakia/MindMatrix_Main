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
import androidx.compose.ui.graphics.Color
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
        val splashScreen = installSplashScreen()
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
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Global Error Handling via Snackbar
    LaunchedEffect(authState.error) {
        val error = authState.error
        if (error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
            }
        }
    }

    if (authState.isLoading) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    // Determine which nav items to show based on role
    val navItems = when (authState.currentEmployee?.role) {
        com.mindmatrix.employeetracker.data.model.UserRole.ADMIN -> NavigationItems.adminItems
        com.mindmatrix.employeetracker.data.model.UserRole.LEAD -> NavigationItems.leadItems
        else -> NavigationItems.employeeItems
    }

    // Screens where bottom nav should be hidden
    val showBottomBar = authState.isLoggedIn && currentRoute != Screen.Login.route
        && currentRoute != Screen.EmployeeDetail.route
        && currentRoute != Screen.Leaderboard.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    for (screen in navItems) {
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                val icon = screen.icon
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            label = { 
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                ) 
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}
