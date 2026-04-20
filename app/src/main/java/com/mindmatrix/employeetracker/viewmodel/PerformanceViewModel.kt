package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val performanceRepository: IPerformanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PerformanceState())
    val state: StateFlow<PerformanceState> = _state.asStateFlow()

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
            val leaderboard = performanceRepository.getLeaderboard()
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

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
