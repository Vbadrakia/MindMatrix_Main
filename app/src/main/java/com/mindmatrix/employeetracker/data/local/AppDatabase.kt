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
        AttendanceRecord::class,
        com.mindmatrix.employeetracker.data.model.Department::class,
        com.mindmatrix.employeetracker.data.model.Document::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun taskDao(): TaskDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun departmentDao(): com.mindmatrix.employeetracker.data.local.dao.DepartmentDao
    abstract fun documentDao(): com.mindmatrix.employeetracker.data.local.dao.DocumentDao

    companion object {
        const val DATABASE_NAME = "employee_tracker_db"
    }
}
