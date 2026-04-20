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
