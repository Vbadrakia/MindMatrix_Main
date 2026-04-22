package com.mindmatrix.employeetracker.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.model.AnalyticsSnapshot
import com.mindmatrix.employeetracker.data.model.DepartmentAverage
import com.mindmatrix.employeetracker.data.model.EmployeeRatingPoint
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import com.mindmatrix.employeetracker.data.model.MonthlyTrendPoint
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IPerformanceRepository {
    private companion object {
        const val TAG = "PerformanceRepository"
    }

    private val collection = firestore.collection("performance")
    private val legacyCollection = firestore.collection("performance_reviews")

    override fun getReviewsForEmployee(employeeId: String): Flow<List<PerformanceReview>> = callbackFlow {
        val listener = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
            val reviews = snapshot?.documents?.map { doc ->
                PerformanceReview.fromMap(doc.id, doc.data ?: emptyMap())
            }?.filter { it.employeeId == employeeId } ?: emptyList()
            trySend(reviews)
        }

        awaitClose { listener.remove() }
    }

    override fun getAllReviews(): Flow<List<PerformanceReview>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reviews = snapshot?.documents?.map { doc ->
                PerformanceReview.fromMap(doc.id, doc.data ?: emptyMap())
            } ?: emptyList()
            trySend(reviews)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addReview(review: PerformanceReview): Result<String> = try {
        val normalized = review.withCalculatedScores()
        val docRef = if (normalized.id.isNotBlank()) collection.document(normalized.id) else collection.document()
        val finalReview = normalized.copy(id = docRef.id)
        docRef.set(finalReview.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateReview(review: PerformanceReview): Result<Unit> = try {
        val normalized = review.withCalculatedScores()
        collection.document(normalized.id).set(normalized.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> = try {
        collection.document(reviewId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAverageScoreForEmployee(employeeId: String): Double = try {
        val snapshot = collection
            .whereEqualTo("employee_id", employeeId)
            .get()
            .await()
        val legacySnapshot = legacyCollection
            .whereEqualTo("employeeId", employeeId)
            .get()
            .await()

        val reviews = (snapshot.documents + legacySnapshot.documents).map { doc ->
            PerformanceReview.fromMap(doc.id, doc.data ?: emptyMap())
        }.distinctBy { it.id }
        if (reviews.isNotEmpty()) reviews.map { it.overallRating }.average() else 0.0
    } catch (e: Exception) {
        Log.e(TAG, "Failed to compute average score for employee $employeeId", e)
        0.0
    }

    override suspend fun getLeaderboard(): List<LeaderboardEntry> {
        val analytics = getAnalyticsSnapshot()
        return analytics.topPerformers + analytics.lowPerformers
    }

    override suspend fun getDepartmentAverages(): List<DepartmentAverage> {
        return getAnalyticsSnapshot().departmentDistribution
    }

    override suspend fun getAnalyticsSnapshot(
        department: String?,
        startDate: String?,
        endDate: String?
    ): AnalyticsSnapshot = try {
        val reviewsSnapshot = collection.get().await().documents + legacyCollection.get().await().documents
        val employeesSnapshot = firestore.collection("employees").get().await()

        val employees = employeesSnapshot.documents.associate { doc ->
            val data = doc.data ?: emptyMap<String, Any?>()
            val name = data["name"] as? String ?: "Unknown"
            val dept = data["department"] as? String ?: "Unknown"
            doc.id to (name to dept)
        }

        val start = parseIsoDate(startDate)
        val end = parseIsoDate(endDate)

        val filteredReviews = reviewsSnapshot
            .map { PerformanceReview.fromMap(it.id, it.data ?: emptyMap()) }
            .distinctBy { it.id }
            .filter { review ->
                val employeeDept = employees[review.employeeId]?.second
                val deptMatch = department.isNullOrBlank() || employeeDept == department
                val reviewDate = parseDate(review.date)
                val startMatch = start == null || reviewDate == null || !reviewDate.isBefore(start)
                val endMatch = end == null || reviewDate == null || !reviewDate.isAfter(end)
                deptMatch && startMatch && endMatch
            }

        val byEmployee = filteredReviews.groupBy { it.employeeId }
        val employeeAverages = byEmployee.map { (employeeId, employeeReviews) ->
            val employeeMeta = employees[employeeId]
            EmployeeRatingPoint(
                employeeId = employeeId,
                employeeName = employeeMeta?.first ?: "Unknown",
                department = employeeMeta?.second ?: "Unknown",
                averageRating = employeeReviews.map { it.overallRating }.average()
            )
        }.sortedByDescending { it.averageRating }

        val deptDistribution = employeeAverages
            .groupBy { it.department }
            .map { (dept, entries) ->
                DepartmentAverage(
                    department = dept,
                    averageScore = entries.map { it.averageRating }.average(),
                    employeeCount = entries.size
                )
            }
            .sortedByDescending { it.averageScore }

        val monthTrend = filteredReviews
            .groupBy {
                parseDate(it.date)?.let { date ->
                    YearMonth.of(date.year, date.month)
                }
            }
            .filterKeys { it != null }
            .toSortedMap()
            .map { (yearMonth, reviews) ->
                MonthlyTrendPoint(
                    month = yearMonth?.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())) ?: "",
                    averageRating = reviews.map { it.overallRating }.average()
                )
            }

        val leaderboard = employeeAverages.mapIndexed { index, point ->
            LeaderboardEntry(
                employeeId = point.employeeId,
                employeeName = point.employeeName,
                department = point.department,
                averageScore = point.averageRating,
                rank = index + 1
            )
        }

        AnalyticsSnapshot(
            employeeAverages = employeeAverages,
            departmentDistribution = deptDistribution,
            monthlyTrend = monthTrend,
            topPerformers = leaderboard.take(3),
            lowPerformers = leaderboard.takeLast(3).reversed()
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load analytics snapshot", e)
        AnalyticsSnapshot()
    }

    override suspend fun syncPerformanceData(): Result<Unit> = Result.success(Unit)

    private fun parseIsoDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null

        val iso = try {
            LocalDate.parse(raw)
        } catch (_: DateTimeParseException) {
            null
        }
        if (iso != null) return iso

        return try {
            LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
