package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.AnalyticsSnapshot
import com.mindmatrix.employeetracker.data.model.DepartmentAverage
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import com.mindmatrix.employeetracker.data.model.PerformanceReview
import com.mindmatrix.employeetracker.data.repository.IPerformanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceState(
    val reviews: List<PerformanceReview> = emptyList(),
    val averageScore: Double = 0.0,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val departmentAverages: List<DepartmentAverage> = emptyList(),
    val analytics: AnalyticsSnapshot = AnalyticsSnapshot(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val performanceRepository: IPerformanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PerformanceState())
    val state: StateFlow<PerformanceState> = _state.asStateFlow()

    fun loadAllReviews() {
        viewModelScope.launch {
            performanceRepository.syncPerformanceData()
            _state.value = _state.value.copy(isLoading = true)
            performanceRepository.getAllReviews().collect { reviews ->
                _state.value = _state.value.copy(
                    reviews = reviews,
                    isLoading = false
                )
            }
        }
    }

    fun loadReviewsForEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            performanceRepository.getReviewsForEmployee(employeeId).collect { reviews ->
                _state.value = _state.value.copy(
                    reviews = reviews,
                    isLoading = false
                )
            }
        }
    }

    fun loadAverageScore(employeeId: String) {
        viewModelScope.launch {
            val avg = performanceRepository.getAverageScoreForEmployee(employeeId)
            _state.value = _state.value.copy(averageScore = avg)
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val leaderboard = performanceRepository.getLeaderboard().sortedByDescending { it.averageScore }
            _state.value = _state.value.copy(
                leaderboard = leaderboard,
                isLoading = false
            )
        }
    }

    fun loadDepartmentAverages() {
        viewModelScope.launch {
            val averages = performanceRepository.getDepartmentAverages()
            _state.value = _state.value.copy(departmentAverages = averages)
        }
    }

    fun loadAnalytics(
        department: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val snapshot = performanceRepository.getAnalyticsSnapshot(department, startDate, endDate)
            _state.value = _state.value.copy(
                analytics = snapshot,
                departmentAverages = snapshot.departmentDistribution,
                leaderboard = snapshot.employeeAverages.mapIndexed { index, point ->
                    LeaderboardEntry(
                        employeeId = point.employeeId,
                        employeeName = point.employeeName,
                        department = point.department,
                        averageScore = point.averageRating,
                        rank = index + 1
                    )
                },
                isLoading = false
            )
        }
    }

    fun addReview(review: PerformanceReview) {
        viewModelScope.launch {
            performanceRepository.addReview(review).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateReview(review: PerformanceReview) {
        viewModelScope.launch {
            performanceRepository.updateReview(review).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            performanceRepository.deleteReview(reviewId).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun approveReview(reviewId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val review = _state.value.reviews.find { it.id == reviewId }
            if (review != null) {
                val updatedReview = review.copy(status = com.mindmatrix.employeetracker.data.model.ReviewStatus.APPROVED)
                performanceRepository.updateReview(updatedReview).onSuccess {
                    // Update local state immediately for better UX
                    _state.value = _state.value.copy(
                        reviews = _state.value.reviews.map { if (it.id == reviewId) updatedReview else it },
                        isLoading = false
                    )
                }.onFailure { e ->
                    _state.value = _state.value.copy(error = e.message, isLoading = false)
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
