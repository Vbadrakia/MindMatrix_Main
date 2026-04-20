package com.mindmatrix.employeetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents user roles in the Employee Tracker system.
 */
enum class UserRole {
    ADMIN,
    LEAD,
    EMPLOYEE
}

/**
 * Represents an employee/user in the system.
 * Maps to Firestore "employees" collection and Room "employees" table.
 */
@Entity(
    tableName = "employees",
    indices = [Index(value = ["email"], unique = true), Index(value = ["department"]), Index(value = ["managerId"])]
)
data class Employee(
    @PrimaryKey
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.EMPLOYEE,
    val department: String = "",
    val designation: String = "",
    val phone: String = "",
    val joinDate: String = "",
    val profileImageUrl: String = "",
    val isActive: Boolean = true,
    val managerId: String = ""
) {
    /**
     * Convert to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "email" to email,
        "name" to name,
        "role" to role.name,
        "department" to department,
        "designation" to designation,
        "phone" to phone,
        "joinDate" to joinDate,
        "profileImageUrl" to profileImageUrl,
        "isActive" to isActive,
        "managerId" to managerId
    )

    companion object {
        /**
         * Create an Employee from a Firestore document map.
         */
        fun fromMap(id: String, map: Map<String, Any?>): Employee = Employee(
            id = id,
            email = map["email"] as? String ?: "",
            name = map["name"] as? String ?: "",
            role = try {
                UserRole.valueOf(map["role"] as? String ?: "EMPLOYEE")
            } catch (_: Exception) {
                UserRole.EMPLOYEE
            },
            department = map["department"] as? String ?: "",
            designation = map["designation"] as? String ?: "",
            phone = map["phone"] as? String ?: "",
            joinDate = map["joinDate"] as? String ?: "",
            profileImageUrl = map["profileImageUrl"] as? String ?: "",
            isActive = map["isActive"] as? Boolean ?: true,
            managerId = map["managerId"] as? String ?: ""
        )
    }
}
