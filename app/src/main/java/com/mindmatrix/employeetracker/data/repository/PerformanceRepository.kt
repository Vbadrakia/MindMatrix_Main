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
    private val collection = firestore.collection("performance_reviews")

    override fun getReviewsForEmployee(employeeId: String): Flow<List<PerformanceReview>> = callbackFlow {
        val listener = collection
            .whereEqualTo("employeeId", employeeId)
            .addSnapshotListener { snapshot, error ->
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

    override fun getAllReviews(): Flow<List<PerformanceReview>> = callbackFlow {
        val listener = collection
            .addSnapshotListener { snapshot, error ->
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
        val docRef = collection.add(review.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateReview(review: PerformanceReview): Result<Unit> = try {
        collection.document(review.id).set(review.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAverageScoreForEmployee(employeeId: String): Double = try {
        val snapshot = collection
            .whereEqualTo("employeeId", employeeId)
            .get()
            .await()
        val reviews = snapshot.documents.mapNotNull { doc ->
            (doc.data?.get("weightedScore") as? Number)?.toDouble()
                ?: (doc.data?.get("overallScore") as? Number)?.toDouble() // Fallback
        }
        if (reviews.isNotEmpty()) reviews.average() else 0.0
    } catch (e: Exception) {
        0.0
    }

    override suspend fun getLeaderboard(): List<LeaderboardEntry> = try {
        val reviewsSnapshot = collection.get().await()
        val employeesSnapshot = firestore.collection("employees").get().await()

        val employeeMap = employeesSnapshot.documents.associate { doc ->
            doc.id to Pair(
                doc.data?.get("name") as? String ?: "",
                doc.data?.get("department") as? String ?: ""
            )
        }

        val scoresByEmployee = reviewsSnapshot.documents
            .groupBy { it.data?.get("employeeId") as? String ?: "" }
            .mapValues { (_, docs) ->
                docs.mapNotNull { 
                    (it.data?.get("weightedScore") as? Number)?.toDouble()
                        ?: (it.data?.get("overallScore") as? Number)?.toDouble()
                }.average()
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
        val reviewsSnapshot = collection.get().await()
        val employeesSnapshot = firestore.collection("employees").get().await()

        val employeeDeptMap = employeesSnapshot.documents.associate { doc ->
            doc.id to (doc.data?.get("department") as? String ?: "Unknown")
        }

        val reviewsByDept = reviewsSnapshot.documents
            .groupBy { doc ->
                val empId = doc.data?.get("employeeId") as? String ?: ""
                employeeDeptMap[empId] ?: "Unknown"
            }

        reviewsByDept.map { (dept, docs) ->
            val scores = docs.mapNotNull { 
                (it.data?.get("weightedScore") as? Number)?.toDouble()
                    ?: (it.data?.get("overallScore") as? Number)?.toDouble()
            }
            DepartmentAverage(
                department = dept,
                averageScore = if (scores.isNotEmpty()) scores.average() else 0.0,
                employeeCount = docs.map { it.data?.get("employeeId") }.distinct().size
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun syncPerformanceData(): Result<Unit> = Result.success(Unit) // No longer needed with real-time sync
}
