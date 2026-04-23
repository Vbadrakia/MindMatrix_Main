package com.mindmatrix.employeetracker.data.model


data class Document(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val ownerId: String = "",
    val type: String = "OTHER", // PDF, Image, etc.
    val uploadDate: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "url" to url,
        "ownerId" to ownerId,
        "type" to type,
        "uploadDate" to uploadDate
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Document = Document(
            id = id,
            name = map["name"] as? String ?: "",
            url = map["url"] as? String ?: "",
            ownerId = map["ownerId"] as? String ?: "",
            type = map["type"] as? String ?: "OTHER",
            uploadDate = map["uploadDate"] as? String ?: ""
        )
    }
}
