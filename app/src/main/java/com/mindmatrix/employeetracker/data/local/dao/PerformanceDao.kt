package com.mindmatrix.employeetracker.data.local.dao

import androidx.room.*
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceDao {
    @Query("SELECT * FROM performance_reviews")
    fun getAllReviews(): Flow<List<PerformanceReview>>

    @Query("SELECT * FROM performance_reviews WHERE employee_id = :employeeId")
    fun getReviewsByEmployee(employeeId: String): Flow<List<PerformanceReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: PerformanceReview)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<PerformanceReview>)

    @Update
    suspend fun updateReview(review: PerformanceReview)

    @Query("DELETE FROM performance_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    @Query("DELETE FROM performance_reviews")
    suspend fun deleteAllReviews()
}
