package com.mindmatrix.employeetracker.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object Employees : Screen("employees")
    object EmployeeDetail : Screen("employee_detail/{employeeId}") {
        fun createRoute(employeeId: String) = "employee_detail/$employeeId"
    }
    object Tasks : Screen("tasks")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object Attendance : Screen("attendance")
    object Performance : Screen("performance")
    object Analytics : Screen("analytics")
    object Reports : Screen("reports")
    object Leaderboard : Screen("leaderboard")
    object Notifications : Screen("notifications")
    object Departments : Screen("departments")
    object Settings : Screen("settings")
}
