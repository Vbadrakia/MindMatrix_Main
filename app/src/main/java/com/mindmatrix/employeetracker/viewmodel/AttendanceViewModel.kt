package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.AttendanceRecord
import com.mindmatrix.employeetracker.data.model.AttendanceStatus
import com.mindmatrix.employeetracker.data.model.StatusCount
import com.mindmatrix.employeetracker.data.repository.IAttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AttendanceState(
    val records: List<AttendanceRecord> = emptyList(),
    val todayRecord: AttendanceRecord? = null,
    val summary: List<StatusCount> = emptyList(),
    val isLoading: Boolean = false,
    val isCheckedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: IAttendanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceState())
    val state: StateFlow<AttendanceState> = _state.asStateFlow()

    fun loadAttendanceForEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            attendanceRepository.getAttendanceForEmployee(employeeId).collect { records ->
                _state.value = _state.value.copy(
                    records = records,
                    isLoading = false
                )
            }
        }
    }

    fun checkTodayAttendance(employeeId: String) {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val record = attendanceRepository.getTodayAttendanceForEmployee(employeeId, today)
            _state.value = _state.value.copy(
                todayRecord = record,
                isCheckedIn = record?.checkInTime?.isNotBlank() == true && record.checkOutTime.isBlank()
            )
        }
    }

    fun checkIn(employeeId: String) {
        viewModelScope.launch {
            val now = LocalTime.now()
            val today = LocalDate.now()
            val status = if (now.hour >= 10) AttendanceStatus.LATE else AttendanceStatus.PRESENT

            val record = AttendanceRecord(
                employeeId = employeeId,
                date = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                checkInTime = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                status = status
            )
            attendanceRepository.markAttendance(record).onSuccess {
                checkTodayAttendance(employeeId)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun checkOut(employeeId: String) {
        viewModelScope.launch {
            val todayRecord = _state.value.todayRecord ?: return@launch
            val now = LocalTime.now()
            val updatedRecord = todayRecord.copy(
                checkOutTime = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                hoursWorked = calculateHours(todayRecord.checkInTime, now.format(DateTimeFormatter.ofPattern("HH:mm")))
            )
            attendanceRepository.updateAttendance(updatedRecord).onSuccess {
                checkTodayAttendance(employeeId)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun loadAttendanceSummary(employeeId: String) {
        viewModelScope.launch {
            val summary = attendanceRepository.getAttendanceSummary(employeeId)
            _state.value = _state.value.copy(summary = summary)
        }
    }

    private fun calculateHours(checkIn: String, checkOut: String): Double {
        return try {
            val inTime = LocalTime.parse(checkIn, DateTimeFormatter.ofPattern("HH:mm"))
            val outTime = LocalTime.parse(checkOut, DateTimeFormatter.ofPattern("HH:mm"))
            val minutes = java.time.Duration.between(inTime, outTime).toMinutes()
            minutes / 60.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
