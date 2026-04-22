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
    val date: String = "",
    val qualityScore: Int = 0,
    val timelinessScore: Int = 0,
    val attendanceScore: Int = 0,
    val communicationScore: Int = 0,
    val innovationScore: Int = 0,
    val overallRating: Double = 0.0,
    val remarks: String = "",
    // Backward-compatible fields currently used in app flows.
    val reviewerId: String = "",
    val period: String = "",
    val reviewDate: String = "",
    val productivityScore: Int = 0,
    val softSkillsScore: Int = 0,
    val teamworkScore: Int = 0,
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
        val normalizedTimeliness = if (timelinessScore == 0 && productivityScore != 0) productivityScore else timelinessScore
        val normalizedCommunication = if (communicationScore == 0 && softSkillsScore != 0) softSkillsScore else communicationScore
        val normalizedInnovation = if (innovationScore == 0 && teamworkScore != 0) teamworkScore else innovationScore

        val avg = (qualityScore + normalizedTimeliness + attendanceScore + normalizedCommunication + normalizedInnovation) / 5.0
        return this.copy(
            date = if (date.isNotBlank()) date else reviewDate,
            reviewDate = if (reviewDate.isNotBlank()) reviewDate else date,
            timelinessScore = normalizedTimeliness,
            communicationScore = normalizedCommunication,
            innovationScore = normalizedInnovation,
            overallRating = avg,
            rawScore = avg,
            weightedScore = avg
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "employee_id" to employeeId,
        "date" to date.ifBlank { reviewDate },
        "quality_score" to qualityScore,
        "timeliness_score" to timelinessScore,
        "attendance_score" to attendanceScore,
        "communication_score" to communicationScore,
        "innovation_score" to innovationScore,
        "overall_rating" to overallRating,
        "remarks" to remarks.ifBlank { comments },
        // Backward-compatible fields
        "employeeId" to employeeId,
        "reviewerId" to reviewerId,
        "reviewDate" to reviewDate.ifBlank { date },
        "period" to period,
        "productivityScore" to timelinessScore,
        "attendanceScore" to attendanceScore,
        "softSkillsScore" to communicationScore,
        "teamworkScore" to innovationScore,
        "rawScore" to overallRating,
        "weightedScore" to overallRating,
        "status" to status.name,
        "comments" to remarks.ifBlank { comments },
        "goals" to goals,
        "strengths" to strengths,
        "areasForImprovement" to areasForImprovement
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): PerformanceReview = PerformanceReview(
            id = id,
            employeeId = (map["employee_id"] as? String ?: map["employeeId"] as? String ?: ""),
            date = (map["date"] as? String ?: map["reviewDate"] as? String ?: ""),
            qualityScore = (map["quality_score"] as? Number)?.toInt()
                ?: (map["qualityScore"] as? Number)?.toInt() ?: 0,
            timelinessScore = (map["timeliness_score"] as? Number)?.toInt()
                ?: (map["productivityScore"] as? Number)?.toInt() ?: 0,
            attendanceScore = (map["attendance_score"] as? Number)?.toInt()
                ?: (map["attendanceScore"] as? Number)?.toInt() ?: 0,
            communicationScore = (map["communication_score"] as? Number)?.toInt()
                ?: (map["softSkillsScore"] as? Number)?.toInt() ?: 0,
            innovationScore = (map["innovation_score"] as? Number)?.toInt()
                ?: (map["teamworkScore"] as? Number)?.toInt() ?: 0,
            overallRating = (map["overall_rating"] as? Number)?.toDouble()
                ?: (map["weightedScore"] as? Number)?.toDouble()
                ?: (map["rawScore"] as? Number)?.toDouble()
                ?: 0.0,
            remarks = (map["remarks"] as? String ?: map["comments"] as? String ?: ""),
            reviewerId = map["reviewerId"] as? String ?: "",
            reviewDate = (map["reviewDate"] as? String ?: map["date"] as? String ?: ""),
            period = map["period"] as? String ?: "",
            productivityScore = (map["productivityScore"] as? Number)?.toInt()
                ?: (map["timeliness_score"] as? Number)?.toInt() ?: 0,
            softSkillsScore = (map["softSkillsScore"] as? Number)?.toInt()
                ?: (map["communication_score"] as? Number)?.toInt() ?: 0,
            teamworkScore = (map["teamworkScore"] as? Number)?.toInt()
                ?: (map["innovation_score"] as? Number)?.toInt() ?: 0,
            rawScore = (map["rawScore"] as? Number)?.toDouble()
                ?: (map["overall_rating"] as? Number)?.toDouble() ?: 0.0,
            weightedScore = (map["weightedScore"] as? Number)?.toDouble()
                ?: (map["overall_rating"] as? Number)?.toDouble() ?: 0.0,
            status = try {
                ReviewStatus.valueOf(map["status"] as? String ?: "APPROVED")
            } catch (_: Exception) {
                ReviewStatus.APPROVED
            },
            comments = (map["comments"] as? String ?: map["remarks"] as? String ?: ""),
            goals = map["goals"] as? String ?: "",
            strengths = map["strengths"] as? String ?: "",
            areasForImprovement = map["areasForImprovement"] as? String ?: ""
        ).withCalculatedScores()
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
