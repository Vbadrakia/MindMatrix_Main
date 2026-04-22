package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.model.DepartmentAverage
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import com.mindmatrix.employeetracker.data.model.AnalyticsSnapshot
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import kotlinx.coroutines.flow.Flow

interface IPerformanceRepository {
    fun getReviewsForEmployee(employeeId: String): Flow<List<PerformanceReview>>
    fun getAllReviews(): Flow<List<PerformanceReview>>
    suspend fun addReview(review: PerformanceReview): Result<String>
    suspend fun updateReview(review: PerformanceReview): Result<Unit>
    suspend fun deleteReview(reviewId: String): Result<Unit>
    suspend fun getAverageScoreForEmployee(employeeId: String): Double
    suspend fun getLeaderboard(): List<LeaderboardEntry>
    suspend fun getDepartmentAverages(): List<DepartmentAverage>
    suspend fun getAnalyticsSnapshot(
        department: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): AnalyticsSnapshot
    suspend fun syncPerformanceData(): Result<Unit>
}
