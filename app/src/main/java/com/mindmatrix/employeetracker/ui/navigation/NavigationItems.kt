package com.mindmatrix.employeetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val route: String,
    val icon: ImageVector?
)

object NavigationItems {
    val adminItems = listOf(
        NavigationItem("Home", Screen.Dashboard.route, Icons.Default.Home),
        NavigationItem("Employees", Screen.Employees.route, Icons.Default.People),
        NavigationItem("Tasks", Screen.Tasks.route, Icons.Default.Assignment),
        NavigationItem("Reports", Screen.Reports.route, Icons.Default.BarChart),
        NavigationItem("Settings", Screen.Settings.route, Icons.Default.Settings)
    )

    val leadItems = listOf(
        NavigationItem("Home", Screen.Dashboard.route, Icons.Default.Home),
        NavigationItem("My Team", Screen.Employees.route, Icons.Default.People),
        NavigationItem("Tasks", Screen.Tasks.route, Icons.Default.Assignment),
        NavigationItem("Performance", Screen.Performance.route, Icons.Default.TrendingUp),
        NavigationItem("Settings", Screen.Settings.route, Icons.Default.Settings)
    )

    val employeeItems = listOf(
        NavigationItem("Home", Screen.Dashboard.route, Icons.Default.Home),
        NavigationItem("My Tasks", Screen.Tasks.route, Icons.Default.Assignment),
        NavigationItem("Attendance", Screen.Attendance.route, Icons.Default.CalendarMonth),
        NavigationItem("Performance", Screen.Performance.route, Icons.Default.TrendingUp),
        NavigationItem("Settings", Screen.Settings.route, Icons.Default.Settings)
    )
}
