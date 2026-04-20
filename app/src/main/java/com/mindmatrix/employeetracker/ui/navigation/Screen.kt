package com.mindmatrix.employeetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines all navigation routes in the app.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Login : Screen("login", "Login")
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    data object Employees : Screen("employees", "Employees", Icons.Filled.People)
    data object EmployeeDetail : Screen("employee_detail/{employeeId}", "Employee Detail") {
        fun createRoute(employeeId: String) = "employee_detail/$employeeId"
    }
    data object Tasks : Screen("tasks", "Tasks", Icons.Filled.Assignment)
    data object Attendance : Screen("attendance", "Attendance", Icons.Filled.EventAvailable)
    data object Performance : Screen("performance", "Performance", Icons.Filled.TrendingUp)
    data object Reports : Screen("reports", "Reports", Icons.Filled.Assessment)
    data object Leaderboard : Screen("leaderboard", "Leaderboard", Icons.Filled.Leaderboard)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

/**
 * Bottom navigation items for each role.
 */
object NavigationItems {
    val adminItems = listOf(
        Screen.Dashboard,
        Screen.Employees,
        Screen.Tasks,
        Screen.Reports,
        Screen.Settings
    )

    val leadItems = listOf(
        Screen.Dashboard,
        Screen.Employees,
        Screen.Tasks,
        Screen.Reports,
        Screen.Settings
    )

    val employeeItems = listOf(
        Screen.Dashboard,
        Screen.Tasks,
        Screen.Attendance,
        Screen.Performance,
        Screen.Settings
    )
}
