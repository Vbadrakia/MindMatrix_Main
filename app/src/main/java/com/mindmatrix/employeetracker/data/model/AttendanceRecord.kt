package com.mindmatrix.employeetracker.data.model


/**
 * Represents an attendance record for an employee.
 * Maps to Firestore "attendance" collection and Room "attendance" table.
 */
data class AttendanceRecord(
    val id: String = "",
    val employeeId: String = "",
    val date: String = "",
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val status: AttendanceStatus = AttendanceStatus.ABSENT,
    val hoursWorked: Double = 0.0,
    val notes: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "employeeId" to employeeId,
        "date" to date,
        "checkInTime" to checkInTime,
        "checkOutTime" to checkOutTime,
        "status" to status.name,
        "hoursWorked" to hoursWorked,
        "notes" to notes
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): AttendanceRecord = AttendanceRecord(
            id = id,
            employeeId = map["employeeId"] as? String ?: "",
            date = map["date"] as? String ?: "",
            checkInTime = map["checkInTime"] as? String ?: "",
            checkOutTime = map["checkOutTime"] as? String ?: "",
            status = try {
                AttendanceStatus.valueOf(map["status"] as? String ?: "ABSENT")
            } catch (_: Exception) {
                AttendanceStatus.ABSENT
            },
            hoursWorked = (map["hoursWorked"] as? Number)?.toDouble() ?: 0.0,
            notes = map["notes"] as? String ?: ""
        )
    }
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    LEAVE,
    HALF_DAY
}
