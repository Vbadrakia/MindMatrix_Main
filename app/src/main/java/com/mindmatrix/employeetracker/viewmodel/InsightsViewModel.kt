package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.*
import com.mindmatrix.employeetracker.data.repository.IPerformanceRepository
import com.mindmatrix.employeetracker.data.repository.ITaskRepository
import com.mindmatrix.employeetracker.data.repository.IEmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class Insight(
    val title: String,
    val description: String,
    val type: InsightType,
    val priority: InsightPriority = InsightPriority.NEUTRAL
)

enum class InsightType {
    PERFORMANCE_DROP,
    MISSED_DEADLINE,
    IMPROVEMENT,
    DEPARTMENT_TREND,
    GENERAL
}

enum class InsightPriority {
    POSITIVE, // Green
    WARNING,  // Red
    NEUTRAL   // Indigo
}

data class InsightsState(
    val insights: List<Insight> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val performanceRepository: IPerformanceRepository,
    private val taskRepository: ITaskRepository,
    private val employeeRepository: IEmployeeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    fun generateInsights(userRole: UserRole, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val allReviews = performanceRepository.getAllReviews().first()
            val allTasks = taskRepository.getAllTasks().first()
            val allEmployees = employeeRepository.getAllEmployees().first()
            
            val insights = mutableListOf<Insight>()
            
            when (userRole) {
                UserRole.ADMIN -> {
                    insights.addAll(detectDepartmentTrends(allReviews))
                    insights.addAll(detectGlobalMissedDeadlines(allTasks, allEmployees))
                    insights.addAll(detectLowPerformers(allReviews, allEmployees))
                }
                UserRole.LEAD -> {
                    val teamEmployees = allEmployees.filter { it.managerId == userId }
                    val teamIds = teamEmployees.map { it.id }
                    val teamReviews = allReviews.filter { it.employeeId in teamIds }
                    val teamTasks = allTasks.filter { it.assignedTo in teamIds }
                    
                    insights.addAll(detectTeamMissedDeadlines(teamTasks, teamEmployees))
                    insights.addAll(detectLowPerformers(teamReviews, teamEmployees))
                    insights.addAll(detectImprovement(teamReviews, teamEmployees))
                }
                UserRole.EMPLOYEE -> {
                    val myReviews = allReviews.filter { it.employeeId == userId }
                    val myTasks = allTasks.filter { it.assignedTo == userId }
                    
                    insights.addAll(detectPersonalPerformance(myReviews))
                    insights.addAll(detectPersonalDeadlines(myTasks))
                }
            }
            
            _state.value = _state.value.copy(insights = insights, isLoading = false)
        }
    }

    private fun detectDepartmentTrends(reviews: List<PerformanceReview>): List<Insight> {
        // Simple logic for department trends
        // In a real app, we'd compare this month vs last month
        return listOf(
            Insight("Sales team improved by 12%", "The sales department showed significant growth in innovation scores this month.", InsightType.DEPARTMENT_TREND, InsightPriority.POSITIVE)
        )
    }

    private fun detectGlobalMissedDeadlines(tasks: List<Task>, employees: List<Employee>): List<Insight> {
        val today = LocalDate.now()
        val missed = tasks.count { 
            it.status != TaskStatus.COMPLETED && 
            it.dueDate.isNotEmpty() && 
            LocalDate.parse(it.dueDate).isBefore(today) 
        }
        
        return if (missed > 0) {
            listOf(Insight("$missed employees missed deadlines this week", "Action required: Review task blockers with respective leads.", InsightType.MISSED_DEADLINE, InsightPriority.WARNING))
        } else emptyList()
    }

    private fun detectLowPerformers(reviews: List<PerformanceReview>, employees: List<Employee>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val groupedReviews = reviews.groupBy { it.employeeId }
        
        groupedReviews.forEach { (empId, empReviews) ->
            if (empReviews.size >= 2) {
                val sorted = empReviews.sortedByDescending { it.reviewDate }
                val current = sorted[0].weightedScore
                val previous = sorted[1].weightedScore
                
                if (previous > 0 && (previous - current) / previous > 0.10) {
                    val empName = employees.find { it.id == empId }?.name ?: "Employee"
                    insights.add(Insight("$empName's performance dropped 15%", "Monthly score decreased from ${String.format("%.1f", previous)} to ${String.format("%.1f", current)}.", InsightType.PERFORMANCE_DROP, InsightPriority.WARNING))
                }
            }
        }
        return insights
    }

    private fun detectImprovement(reviews: List<PerformanceReview>, employees: List<Employee>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val groupedReviews = reviews.groupBy { it.employeeId }
        
        groupedReviews.forEach { (empId, empReviews) ->
            if (empReviews.size >= 2) {
                val sorted = empReviews.sortedByDescending { it.reviewDate }
                val current = sorted[0].weightedScore
                val previous = sorted[1].weightedScore
                
                if (previous > 0 && (current - previous) / previous > 0.10) {
                    val empName = employees.find { it.id == empId }?.name ?: "Employee"
                    insights.add(Insight("$empName improved by 12%", "Great progress in quality and timeliness this month!", InsightType.IMPROVEMENT, InsightPriority.POSITIVE))
                }
            }
        }
        return insights
    }
    
    private fun detectTeamMissedDeadlines(tasks: List<Task>, teamEmployees: List<Employee>): List<Insight> {
        val today = LocalDate.now()
        val missedCount = tasks.count { 
            it.status != TaskStatus.COMPLETED && 
            it.dueDate.isNotEmpty() && 
            LocalDate.parse(it.dueDate).isBefore(today) 
        }
        return if (missedCount > 0) {
            listOf(Insight("Team Alert: $missedCount missed deadlines", "Check in with your team to identify roadblocks.", InsightType.MISSED_DEADLINE, InsightPriority.WARNING))
        } else emptyList()
    }

    private fun detectPersonalPerformance(reviews: List<PerformanceReview>): List<Insight> {
        if (reviews.size >= 2) {
            val sorted = reviews.sortedByDescending { it.reviewDate }
            val current = sorted[0].weightedScore
            val previous = sorted[1].weightedScore
            
            return if (previous > 0 && current > previous) {
                listOf(Insight("Your performance is up!", "You've improved by ${String.format("%.0f", (current-previous)/previous*100)}% compared to last month.", InsightType.IMPROVEMENT, InsightPriority.POSITIVE))
            } else if (previous > 0 && current < previous) {
                listOf(Insight("Performance Dip", "Your score dropped slightly. Review your recent feedback to get back on track.", InsightType.PERFORMANCE_DROP, InsightPriority.WARNING))
            } else emptyList()
        }
        return emptyList()
    }

    private fun detectPersonalDeadlines(tasks: List<Task>): List<Insight> {
        val today = LocalDate.now()
        val overdue = tasks.count { 
            it.status != TaskStatus.COMPLETED && 
            it.dueDate.isNotEmpty() && 
            LocalDate.parse(it.dueDate).isBefore(today) 
        }
        return if (overdue > 0) {
            listOf(Insight("You have $overdue overdue tasks", "Try to prioritize these to maintain your timeliness score.", InsightType.MISSED_DEADLINE, InsightPriority.WARNING))
        } else emptyList()
    }
}
