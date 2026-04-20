package com.mindmatrix.employeetracker.data.local

import androidx.room.TypeConverter
import com.mindmatrix.employeetracker.data.model.*

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(role: String): UserRole = UserRole.valueOf(role)

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus = TaskStatus.valueOf(status)

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority = TaskPriority.valueOf(priority)

    @TypeConverter
    fun fromReviewStatus(status: ReviewStatus): String = status.name

    @TypeConverter
    fun toReviewStatus(status: String): ReviewStatus = ReviewStatus.valueOf(status)

    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toAttendanceStatus(status: String): AttendanceStatus = AttendanceStatus.valueOf(status)
}
