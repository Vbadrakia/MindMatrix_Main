package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.model.DepartmentAverage
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IPerformanceRepository {

    private val reviewsCollection = firestore.collection("performance")
    private val employeesCollection = firestore.collection("employees")

    override fun getReviewsForEmployee(employeeId: String): Flow<List<PerformanceReview>> = callbackFlow {
        val listener = reviewsCollection
            .whereEqualTo("employee_id", employeeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.map { doc ->
                    PerformanceReview.fromMap(doc.id, doc.data ?: emptyMap())
                }.orEmpty()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllReviews(): Flow<List<PerformanceReview>> = callbackFlow {
        val listener = reviewsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reviews = snapshot?.documents?.map { doc ->
                PerformanceReview.fromMap(doc.id, doc.data ?: emptyMap())
            }.orEmpty()
            trySend(reviews)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addReview(review: PerformanceReview): Result<String> = try {
        val id = review.id.ifBlank { reviewsCollection.document().id }
        val payload = review.copy(id = id, lastUpdated = System.currentTimeMillis()).toMap() + ("id" to id)
        reviewsCollection.document(id).set(payload).await()
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateReview(review: PerformanceReview): Result<Unit> = try {
        val payload = review.copy(lastUpdated = System.currentTimeMillis()).toMap() + ("id" to review.id)
        reviewsCollection.document(review.id).set(payload).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAverageScoreForEmployee(employeeId: String): Double = try {
        val snapshot = reviewsCollection.whereEqualTo("employee_id", employeeId).get().await()
        val scores = snapshot.documents.mapNotNull { doc ->
            (doc.data?.get("overall_rating") as? Number)?.toDouble()
                ?: (doc.data?.get("weightedScore") as? Number)?.toDouble()
        }
        if (scores.isNotEmpty()) scores.average() else 0.0
    } catch (_: Exception) {
        0.0
    }

    override suspend fun getLeaderboard(): List<LeaderboardEntry> = try {
        val reviews = reviewsCollection.get().await().documents
        val employees = employeesCollection.get().await().documents

        val employeeMap = employees.associate { doc ->
            doc.id to Pair(
                doc.getString("name") ?: "Unknown",
                doc.getString("department") ?: "Unknown"
            )
        }

        val scoresByEmployee = reviews.groupBy { doc ->
            (doc.data?.get("employee_id") ?: doc.data?.get("employeeId")) as? String ?: ""
        }.mapValues { (_, docs) ->
            docs.mapNotNull {
                (it.data?.get("overall_rating") as? Number)?.toDouble()
                    ?: (it.data?.get("weightedScore") as? Number)?.toDouble()
            }.average()
        }

        scoresByEmployee.entries
            .filter { it.key.isNotBlank() }
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                val (name, dept) = employeeMap[entry.key] ?: ("Unknown" to "Unknown")
                LeaderboardEntry(
                    employeeId = entry.key,
                    employeeName = name,
                    department = dept,
                    averageScore = entry.value,
                    rank = index + 1
                )
            }
    } catch (_: Exception) {
        emptyList()
    }

    override suspend fun getDepartmentAverages(): List<DepartmentAverage> = try {
        val reviews = reviewsCollection.get().await().documents
        val employees = employeesCollection.get().await().documents
        val deptByEmployee = employees.associate { it.id to (it.getString("department") ?: "Unknown") }

        val byDept = reviews.groupBy { doc ->
            val employeeId = (doc.data?.get("employee_id") ?: doc.data?.get("employeeId")) as? String ?: ""
            deptByEmployee[employeeId] ?: "Unknown"
        }

        byDept.map { (dept, docs) ->
            val scores = docs.mapNotNull {
                (it.data?.get("overall_rating") as? Number)?.toDouble()
                    ?: (it.data?.get("weightedScore") as? Number)?.toDouble()
            }
            DepartmentAverage(
                department = dept,
                averageScore = if (scores.isNotEmpty()) scores.average() else 0.0,
                employeeCount = docs.map {
                    (it.data?.get("employee_id") ?: it.data?.get("employeeId")) as? String ?: ""
                }.filter { it.isNotBlank() }.distinct().size
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

    override suspend fun syncPerformanceData(): Result<Unit> = Result.success(Unit)
}
