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
    val contact: String = "",
    val joiningDate: String = "",
    val profileImageUrl: String = "",
    val isActive: Boolean = true,
    val managerId: String = "",
    val badges: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Backward compatibility alias for old field naming in UI/domain layers.
    val phone: String get() = contact
    val joinDate: String get() = joiningDate

    /**
     * Convert to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "email" to email,
        "name" to name,
        "role" to role.name,
        "department" to department,
        "joining_date" to joiningDate,
        "contact" to contact,
        // Backward-compatible fields
        "designation" to designation,
        "phone" to contact,
        "joinDate" to joiningDate,
        "profileImageUrl" to profileImageUrl,
        "isActive" to isActive,
        "managerId" to managerId,
        "badges" to badges,
        "lastUpdated" to lastUpdated
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
            contact = (map["contact"] as? String ?: map["phone"] as? String ?: ""),
            joiningDate = (map["joining_date"] as? String ?: map["joinDate"] as? String ?: ""),
            profileImageUrl = map["profileImageUrl"] as? String ?: "",
            isActive = map["isActive"] as? Boolean ?: true,
            managerId = map["managerId"] as? String ?: "",
            badges = (map["badges"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            lastUpdated = (map["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
