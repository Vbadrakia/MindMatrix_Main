package com.mindmatrix.employeetracker.data.model


/**
 * Represents a performance review for an employee.
 * Maps to Firestore "performance" collection.
 */
data class PerformanceReview(
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
    val areasForImprovement: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val productivityScore: Int
        get() = timelinessScore
    val softSkillsScore: Int
        get() = communicationScore
    val teamworkScore: Int
        get() = innovationScore

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
        "employee_id" to employeeId,
        "reviewerId" to reviewerId,
        "reviewDate" to reviewDate,
        "date" to reviewDate,
        "period" to period,
        "qualityScore" to qualityScore,
        "quality_score" to qualityScore,
        "productivityScore" to timelinessScore,
        "timelinessScore" to timelinessScore,
        "timeliness_score" to timelinessScore,
        "attendanceScore" to attendanceScore,
        "attendance_score" to attendanceScore,
        "softSkillsScore" to communicationScore,
        "communicationScore" to communicationScore,
        "communication_score" to communicationScore,
        "teamworkScore" to innovationScore,
        "innovationScore" to innovationScore,
        "innovation_score" to innovationScore,
        "rawScore" to rawScore,
        "weightedScore" to weightedScore,
        "overall_rating" to weightedScore,
        "status" to status.name,
        "comments" to comments,
        "remarks" to comments,
        "goals" to goals,
        "strengths" to strengths,
        "areasForImprovement" to areasForImprovement,
        "lastUpdated" to lastUpdated
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): PerformanceReview = PerformanceReview(
            id = id,
            employeeId = (map["employeeId"] ?: map["employee_id"]) as? String ?: "",
            reviewerId = map["reviewerId"] as? String ?: "",
            reviewDate = (map["reviewDate"] ?: map["date"]) as? String ?: "",
            period = map["period"] as? String ?: "",
            qualityScore = ((map["qualityScore"] ?: map["quality_score"]) as? Number)?.toInt() ?: 0,
            timelinessScore = ((map["timelinessScore"] ?: map["productivityScore"]) as? Number)?.toInt() ?: 0,
            attendanceScore = ((map["attendanceScore"] ?: map["attendance_score"]) as? Number)?.toInt() ?: 0,
            communicationScore = ((map["communicationScore"] ?: map["softSkillsScore"] ?: map["communication_score"]) as? Number)?.toInt() ?: 0,
            innovationScore = ((map["innovationScore"] ?: map["teamworkScore"] ?: map["innovation_score"]) as? Number)?.toInt() ?: 0,
            rawScore = (map["rawScore"] as? Number)?.toDouble() ?: 0.0,
            weightedScore = ((map["weightedScore"] ?: map["overall_rating"]) as? Number)?.toDouble() ?: 0.0,
            status = try {
                ReviewStatus.valueOf(map["status"] as? String ?: "APPROVED")
            } catch (_: Exception) {
                ReviewStatus.APPROVED
            },
            comments = (map["comments"] ?: map["remarks"]) as? String ?: "",
            goals = map["goals"] as? String ?: "",
            strengths = map["strengths"] as? String ?: "",
            areasForImprovement = map["areasForImprovement"] as? String ?: "",
            lastUpdated = (map["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
