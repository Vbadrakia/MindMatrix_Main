package com.mindmatrix.employeetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.ui.screens.attendance.AttendanceScreen
import com.mindmatrix.employeetracker.ui.screens.dashboard.AdminDashboardScreen
import com.mindmatrix.employeetracker.ui.screens.dashboard.EmployeeDashboardScreen
import com.mindmatrix.employeetracker.ui.screens.dashboard.LeadDashboardScreen
import com.mindmatrix.employeetracker.ui.screens.employees.EmployeeDetailScreen
import com.mindmatrix.employeetracker.ui.screens.employees.EmployeeListScreen
import com.mindmatrix.employeetracker.ui.screens.login.LoginScreen
import com.mindmatrix.employeetracker.ui.screens.performance.PerformanceHistoryScreen
import com.mindmatrix.employeetracker.ui.screens.reports.LeaderboardScreen
import com.mindmatrix.employeetracker.ui.screens.reports.ReportsScreen
import com.mindmatrix.employeetracker.ui.screens.settings.SettingsScreen
import com.mindmatrix.employeetracker.ui.screens.tasks.TaskListScreen
import com.mindmatrix.employeetracker.viewmodel.AuthViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (authState.isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
        enterTransition = {
            fadeIn(animationSpec = tween(500)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(500)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(500)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(500)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            when (authState.currentEmployee?.role) {
                UserRole.ADMIN -> AdminDashboardScreen(
                    onNavigateToEmployees = { navController.navigate(Screen.Employees.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) }
                )
                UserRole.LEAD -> LeadDashboardScreen(
                    onNavigateToEmployees = { navController.navigate(Screen.Employees.route) },
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) }
                )
                else -> EmployeeDashboardScreen(
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) }
                )
            }
        }

        composable(Screen.Employees.route) {
            if (authState.currentEmployee?.role == UserRole.ADMIN || authState.currentEmployee?.role == UserRole.LEAD) {
                EmployeeListScreen(
                    onEmployeeClick = { employeeId ->
                        navController.navigate(Screen.EmployeeDetail.createRoute(employeeId))
                    }
                )
            } else {
                // Redirect or show restricted access
                navController.popBackStack()
            }
        }

        composable(
            route = Screen.EmployeeDetail.route,
            arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            EmployeeDetailScreen(
                employeeId = employeeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Tasks.route) {
            TaskListScreen()
        }

        composable(Screen.Attendance.route) {
            AttendanceScreen()
        }

        composable(Screen.Performance.route) {
            PerformanceHistoryScreen()
        }

        composable(Screen.Reports.route) {
            if (authState.currentEmployee?.role == UserRole.ADMIN || authState.currentEmployee?.role == UserRole.LEAD) {
                ReportsScreen(
                    onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(Screen.Leaderboard.route) {
            if (authState.currentEmployee?.role == UserRole.ADMIN) {
                LeaderboardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    authState.currentEmployee?.id?.let { id ->
                        navController.navigate(Screen.EmployeeDetail.createRoute(id))
                    }
                },
                onNavigateToNotifications = {
                    // Navigate to a notifications screen or show a message
                },
                onNavigateToAppearance = {
                    // Navigate to an appearance/theme screen
                },
                onNavigateToAbout = {
                    // Navigate to an about screen
                }
            )
        }
    }
}
