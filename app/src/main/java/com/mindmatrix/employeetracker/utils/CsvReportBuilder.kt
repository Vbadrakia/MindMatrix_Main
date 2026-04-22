package com.mindmatrix.employeetracker.utils

import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvReportBuilder {
    fun buildLeaderboardCsv(
        entries: List<LeaderboardEntry>,
        employees: List<Employee>,
        selectedCategory: String,
        generatedAt: Date = Date()
    ): String {
        val csvData = StringBuilder()
        val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(generatedAt)

        csvData.append("Employee ID,Employee Name,Department,Average Score,Rank,Category,Date\n")

        entries.forEach { entry ->
            val employee = employees.find { it.name == entry.employeeName }
            csvData.append("${employee?.id ?: "N/A"},")
            csvData.append("${entry.employeeName},")
            csvData.append("${entry.department},")
            csvData.append("${String.format(Locale.getDefault(), "%.2f", entry.averageScore)},")
            csvData.append("${entry.rank},")
            csvData.append("$selectedCategory,")
            csvData.append("$dateStamp\n")
        }

        return csvData.toString()
    }
}
