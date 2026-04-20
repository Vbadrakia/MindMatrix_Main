package com.mindmatrix.employeetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mindmatrix.employeetracker.data.local.dao.AttendanceDao
import com.mindmatrix.employeetracker.data.local.dao.EmployeeDao
import com.mindmatrix.employeetracker.data.local.dao.PerformanceDao
import com.mindmatrix.employeetracker.data.local.dao.TaskDao
import com.mindmatrix.employeetracker.data.model.AttendanceRecord
import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import com.mindmatrix.employeetracker.data.model.Task

@Database(
    entities = [
        Employee::class,
        Task::class,
        PerformanceReview::class,
        AttendanceRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun taskDao(): TaskDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        const val DATABASE_NAME = "employee_tracker_db"
    }
}
