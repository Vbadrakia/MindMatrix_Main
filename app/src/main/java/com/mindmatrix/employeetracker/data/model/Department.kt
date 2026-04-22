package com.mindmatrix.employeetracker.data.model


data class Department(
        val id: String = "",
    val name: String = "",
    val description: String = "",
    val headId: String = "" // Employee ID of the department head
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "headId" to headId
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Department = Department(
            id = id,
            name = map["name"] as? String ?: "",
            description = map["description"] as? String ?: "",
            headId = map["headId"] as? String ?: ""
        )
    }
}
