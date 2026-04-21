package com.mindmatrix.employeetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a performance review for an employee.
 * Maps to Firestore "performance_reviews" collection and Room table.
 */
@Entity(tableName = "performance_reviews")
data class PerformanceReview(
    @PrimaryKey
    val id: String = "",
    val employeeId: String = "",
    val reviewerId: String = "",
    val reviewDate: String = "",
    val period: String = "",
    val qualityScore: Int = 0,
    val timelinessScore: Int = 0,
    val attendanceScore: Int = 0,
    val communicationScore: Int = 0,
    val innovationScore: Int = 0,
    val rawScore: Double = 0.0,
    val weightedScore: Double = 0.0,
    val status: ReviewStatus = ReviewStatus.APPROVED,
    val comments: String = "",
    val goals: String = "",
    val strengths: String = "",
    val areasForImprovement: String = ""
) {
    /**
     * Calculates raw and weighted scores based on performance metrics.
     */
    fun withCalculatedScores(): PerformanceReview {
        val raw = (qualityScore + timelinessScore + attendanceScore + communicationScore + innovationScore).toDouble() / 5.0
        
        // quality * 0.30 + timeliness * 0.25 + attendance * 0.15 + communication * 0.15 + innovation * 0.15
        val weighted = (qualityScore * 0.30) +
                (timelinessScore * 0.25) +
                (attendanceScore * 0.15) +
                (communicationScore * 0.15) +
                (innovationScore * 0.15)
        
        return this.copy(
            rawScore = raw,
            weightedScore = weighted
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "employeeId" to employeeId,
        "reviewerId" to reviewerId,
        "reviewDate" to reviewDate,
        "period" to period,
        "qualityScore" to qualityScore,
        "timelinessScore" to timelinessScore,
        "attendanceScore" to attendanceScore,
        "communicationScore" to communicationScore,
        "innovationScore" to innovationScore,
        "rawScore" to rawScore,
        "weightedScore" to weightedScore,
        "status" to status.name,
        "comments" to comments,
        "goals" to goals,
        "strengths" to strengths,
        "areasForImprovement" to areasForImprovement
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): PerformanceReview = PerformanceReview(
            id = id,
            employeeId = map["employeeId"] as? String ?: "",
            reviewerId = map["reviewerId"] as? String ?: "",
            reviewDate = map["reviewDate"] as? String ?: "",
            period = map["period"] as? String ?: "",
            qualityScore = (map["qualityScore"] as? Number)?.toInt() ?: 0,
            timelinessScore = (map["timelinessScore"] as? Number)?.toInt() ?: 0,
            attendanceScore = (map["attendanceScore"] as? Number)?.toInt() ?: 0,
            communicationScore = (map["communicationScore"] as? Number)?.toInt() ?: 0,
            innovationScore = (map["innovationScore"] as? Number)?.toInt() ?: 0,
            rawScore = (map["rawScore"] as? Number)?.toDouble() ?: 0.0,
            weightedScore = (map["weightedScore"] as? Number)?.toDouble() ?: 0.0,
            status = try {
                ReviewStatus.valueOf(map["status"] as? String ?: "APPROVED")
            } catch (_: Exception) {
                ReviewStatus.APPROVED
            },
            comments = map["comments"] as? String ?: "",
            goals = map["goals"] as? String ?: "",
            strengths = map["strengths"] as? String ?: "",
            areasForImprovement = map["areasForImprovement"] as? String ?: ""
        )
    }
}

/**
 * Represents the status of a performance review.
 */
enum class ReviewStatus {
    REQUESTED,    // By Employee
    DRAFT,        // By Lead
    SUBMITTED,    // Pending Approval
    APPROVED      // Finalized
}
