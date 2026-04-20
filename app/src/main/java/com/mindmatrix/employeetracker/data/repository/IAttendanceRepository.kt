package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.model.AttendanceRecord
import com.mindmatrix.employeetracker.data.model.StatusCount
import kotlinx.coroutines.flow.Flow

interface IAttendanceRepository {
    fun getAttendanceForEmployee(employeeId: String): Flow<List<AttendanceRecord>>
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>
    fun getAllAttendance(): Flow<List<AttendanceRecord>>
    suspend fun markAttendance(record: AttendanceRecord): Result<String>
    suspend fun updateAttendance(record: AttendanceRecord): Result<Unit>
    suspend fun getAttendanceSummary(employeeId: String): List<StatusCount>
    suspend fun getTodayAttendanceForEmployee(employeeId: String, date: String): AttendanceRecord?
    suspend fun syncAttendance(): Result<Unit>
}
