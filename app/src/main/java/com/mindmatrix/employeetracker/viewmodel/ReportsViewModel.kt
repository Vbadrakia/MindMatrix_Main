package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor() : ViewModel() {

    private val _selectedDeptFilter = MutableStateFlow<String?>(null)
    val selectedDeptFilter: StateFlow<String?> = _selectedDeptFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("Overall")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow("Monthly")
    val selectedTimeRange: StateFlow<String> = _selectedTimeRange.asStateFlow()

    fun setSelectedDeptFilter(department: String?) {
        _selectedDeptFilter.value = department
    }

    fun setSelectedCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setSelectedTimeRange(range: String) {
        _selectedTimeRange.value = range
    }

    fun filterLeaderboard(
        leaderboard: List<LeaderboardEntry>,
        isAdmin: Boolean,
        currentDepartment: String?
    ): List<LeaderboardEntry> {
        if (isAdmin) {
            return _selectedDeptFilter.value?.let { dept ->
                leaderboard.filter { it.department == dept }
            } ?: leaderboard
        }
        return leaderboard.filter { it.department == currentDepartment }
    }
}
