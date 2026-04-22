package com.mindmatrix.employeetracker.data.model

/** Used for attendance status distribution in reports. */
data class StatusCount(
    val status: AttendanceStatus = AttendanceStatus.ABSENT,
    val count: Int = 0
)

/** Used for department-wise performance averages in reports. */
data class DepartmentAverage(
    val department: String = "",
    val averageScore: Double = 0.0,
    val employeeCount: Int = 0
)

/** Used for the performance leaderboard. */
data class LeaderboardEntry(
    val employeeId: String = "",
    val employeeName: String = "",
    val department: String = "",
    val averageScore: Double = 0.0,
    val rank: Int = 0
)

data class EmployeeRatingPoint(
    val employeeId: String = "",
    val employeeName: String = "",
    val department: String = "",
    val averageRating: Double = 0.0
)

data class MonthlyTrendPoint(
    val month: String = "",
    val averageRating: Double = 0.0
)

data class AnalyticsSnapshot(
    val employeeAverages: List<EmployeeRatingPoint> = emptyList(),
    val departmentDistribution: List<DepartmentAverage> = emptyList(),
    val monthlyTrend: List<MonthlyTrendPoint> = emptyList(),
    val topPerformers: List<LeaderboardEntry> = emptyList(),
    val lowPerformers: List<LeaderboardEntry> = emptyList()
)
