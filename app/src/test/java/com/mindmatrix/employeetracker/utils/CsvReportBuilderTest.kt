package com.mindmatrix.employeetracker.utils

import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.LeaderboardEntry
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class CsvReportBuilderTest {

    @Test
    fun `buildLeaderboardCsv includes header and entries`() {
        val entries = listOf(
            LeaderboardEntry(
                employeeId = "e1",
                employeeName = "Jane Doe",
                department = "Engineering",
                averageScore = 88.5,
                rank = 1
            )
        )
        val employees = listOf(Employee(id = "e1", name = "Jane Doe"))

        val csv = CsvReportBuilder.buildLeaderboardCsv(
            entries = entries,
            employees = employees,
            selectedCategory = "Overall",
            generatedAt = Date(0)
        )

        assertTrue(csv.contains("Employee ID,Employee Name,Department,Average Score,Rank,Category,Date"))
        assertTrue(csv.contains("e1,Jane Doe,Engineering,88.50,1,Overall,1970-01-01"))
    }
}
