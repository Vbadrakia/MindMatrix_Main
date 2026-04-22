package com.mindmatrix.employeetracker.data.repository

import com.mindmatrix.employeetracker.data.local.dao.EmployeeDao
import com.mindmatrix.employeetracker.data.local.dao.PerformanceDao
import com.mindmatrix.employeetracker.data.model.DepartmentAverage
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceRepository @Inject constructor(
    private val performanceDao: PerformanceDao,
    private val employeeDao: EmployeeDao
) : IPerformanceRepository {

    override fun getReviewsForEmployee(employeeId: String): Flow<List<PerformanceReview>> = 
        performanceDao.getReviewsByEmployee(employeeId)

    override fun getAllReviews(): Flow<List<PerformanceReview>> = 
        performanceDao.getAllReviews()

    override suspend fun addReview(review: PerformanceReview): Result<String> = try {
        val timestampedReview = review.copy(lastUpdated = System.currentTimeMillis())
        val finalReview = timestampedReview.copy(id = review.id.ifBlank { java.util.UUID.randomUUID().toString() })
        performanceDao.insertReview(finalReview)
        Result.success(finalReview.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateReview(review: PerformanceReview): Result<Unit> = try {
        val timestampedReview = review.copy(lastUpdated = System.currentTimeMillis())
        performanceDao.updateReview(timestampedReview)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAverageScoreForEmployee(employeeId: String): Double = try {
        val reviews = performanceDao.getReviewsByEmployee(employeeId).first().map { it.weightedScore }
        if (reviews.isNotEmpty()) reviews.average() else 0.0
    } catch (e: Exception) {
        0.0
    }

    override suspend fun getLeaderboard(): List<LeaderboardEntry> = try {
        val reviews = performanceDao.getAllReviews().first()
        val employees = employeeDao.getAllEmployees().first()
        val employeeMap = employees.associate { it.id to (it.name to it.department) }

        val scoresByEmployee = reviews
            .groupBy { it.employeeId }
            .mapValues { (_, docs) ->
                docs.map { it.weightedScore }.average()
            }

        scoresByEmployee.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                val (name, department) = employeeMap[entry.key] ?: Pair("Unknown", "Unknown")
                LeaderboardEntry(
                    employeeId = entry.key,
                    employeeName = name,
                    department = department,
                    averageScore = entry.value,
                    rank = index + 1
                )
            }
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun getDepartmentAverages(): List<DepartmentAverage> = try {
        val reviews = performanceDao.getAllReviews().first()
        val employeeDeptMap = employeeDao.getAllEmployees().first().associate { it.id to it.department }
        val reviewsByDept = reviews.groupBy { review ->
            employeeDeptMap[review.employeeId] ?: "Unknown"
        }

        reviewsByDept.map { (dept, docs) ->
            val scores = docs.map { it.weightedScore }
            DepartmentAverage(
                department = dept,
                averageScore = if (scores.isNotEmpty()) scores.average() else 0.0,
                employeeCount = docs.map { it.employeeId }.distinct().size
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun syncPerformanceData(): Result<Unit> = try {
        // Room-only mode: no remote sync required.
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
