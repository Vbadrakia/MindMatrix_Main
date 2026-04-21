package com.mindmatrix.employeetracker.util

import com.mindmatrix.employeetracker.data.model.*
import com.mindmatrix.employeetracker.data.repository.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SampleDataGenerator @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val taskRepository: TaskRepository,
    private val attendanceRepository: AttendanceRepository,
    private val performanceRepository: PerformanceRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun seedLastMonthData() {
        // 0. Seed Employees if they don't exist
        val employeeList = seedEmployees()
        val employeeIds = employeeList.filter { it.role != UserRole.ADMIN }.map { it.id }
        val adminId = employeeList.find { it.role == UserRole.ADMIN }?.id ?: "admin_id"

        val lastMonth = LocalDate.now().minusMonths(1)
        val daysInMonth = lastMonth.lengthOfMonth()

        // 1. Generate Attendance for last month
        for (day in 1..daysInMonth) {
            val date = lastMonth.withDayOfMonth(day)
            if (date.dayOfWeek.value in 1..5) { // Weekdays only
                employeeIds.forEach { empId ->
                    val isAbsent = Random.nextInt(100) < 5 // 5% chance of absence
                    if (!isAbsent) {
                        val isLate = Random.nextInt(100) < 15 // 15% chance of being late
                        val checkInHour = if (isLate) 10 else 9
                        val checkInTime = LocalTime.of(checkInHour, Random.nextInt(0, 30))
                        val checkOutTime = LocalTime.of(18, Random.nextInt(0, 59))
                        
                        val record = AttendanceRecord(
                            employeeId = empId,
                            date = date.format(dateFormatter),
                            checkInTime = checkInTime.format(timeFormatter),
                            checkOutTime = checkOutTime.format(timeFormatter),
                            status = if (isLate) AttendanceStatus.LATE else AttendanceStatus.PRESENT,
                            hoursWorked = 8.5 + Random.nextDouble(-0.5, 1.5)
                        )
                        attendanceRepository.markAttendance(record)
                    } else {
                        val record = AttendanceRecord(
                            employeeId = empId,
                            date = date.format(dateFormatter),
                            status = AttendanceStatus.ABSENT
                        )
                        attendanceRepository.markAttendance(record)
                    }
                }
            }
        }

        // 2. Generate Tasks for last month
        val engineeringTasks = listOf("Backend API development", "Bug fixing in Auth module", "Code review for PR #42", "Database migration", "Security audit", "Unit testing", "Sprint planning", "Documentation update")
        val designTasks = listOf("UI redesign for Dashboard", "Logo iteration", "User interview analysis", "Design system update", "High-fidelity prototyping", "Wireframing for new feature", "Iconography set", "Color palette refinement")
        val marketingTasks = listOf("Content calendar planning", "Blog post creation", "Social media campaign", "SEO optimization", "Email newsletter", "Market research", "Ad copy writing", "Analytics report preparation")
        val generalTasks = listOf("Resource allocation", "Budget review", "Stakeholder meeting", "Project roadmap update", "Performance appraisal", "Policy revision")

        employeeList.filter { it.role != UserRole.ADMIN }.forEach { employee ->
            val pool = when (employee.department) {
                "Engineering" -> engineeringTasks
                "Design" -> designTasks
                "Marketing" -> marketingTasks
                else -> generalTasks
            }
            
            repeat(12) { i ->
                val createdDate = lastMonth.withDayOfMonth(Random.nextInt(1, 28))
                val completed = Random.nextInt(100) < 80 // 80% completion rate
                val status = if (completed) TaskStatus.COMPLETED else TaskStatus.OVERDUE
                
                val task = Task(
                    title = "${pool.random()} #$i",
                    description = "Crucial task for ${employee.name} to support ${employee.department} objectives for the month.",
                    assignedTo = employee.id,
                    assignedBy = employee.managerId.ifEmpty { adminId },
                    status = status,
                    priority = TaskPriority.entries.random(),
                    createdAt = createdDate.format(dateFormatter),
                    dueDate = createdDate.plusDays(Random.nextLong(2, 10)).format(dateFormatter),
                    completedAt = if (completed) createdDate.plusDays(Random.nextLong(1, 5)).format(dateFormatter) else ""
                )
                taskRepository.addTask(task)
            }
        }

        // 3. Generate Performance Reviews for last month
        employeeIds.forEach { empId ->
            val quality = Random.nextInt(75, 95)
            val timeliness = Random.nextInt(70, 95)
            val attendance = Random.nextInt(85, 100)
            val communication = Random.nextInt(75, 100)
            val innovation = Random.nextInt(60, 95)
            
            val review = PerformanceReview(
                employeeId = empId,
                reviewerId = adminId,
                reviewDate = LocalDate.now().withDayOfMonth(1).format(dateFormatter),
                period = "${lastMonth.month.name} ${lastMonth.year}",
                qualityScore = quality,
                timelinessScore = timeliness,
                attendanceScore = attendance,
                communicationScore = communication,
                innovationScore = innovation,
                status = ReviewStatus.APPROVED,
                comments = "Consistently delivering high-quality work during ${lastMonth.month.name}.",
                strengths = "Technical proficiency, team collaboration",
                areasForImprovement = "Time estimation"
            ).withCalculatedScores()
            performanceRepository.addReview(review)
        }
    }

    private suspend fun seedEmployees(): List<Employee> {
        val employees = listOf(
            Employee(
                id = "admin_id",
                email = "admin@mindmatrix.com",
                name = "Admin User",
                role = UserRole.ADMIN,
                department = "Management",
                designation = "System Administrator",
                phone = "+1234567890",
                joinDate = "2023-01-01"
            ),
            Employee(
                id = "lead_1",
                email = "lead@mindmatrix.com",
                name = "Sarah Chen",
                role = UserRole.LEAD,
                department = "Engineering",
                designation = "Senior Lead Engineer",
                phone = "+1234567891",
                joinDate = "2023-03-15",
                managerId = "admin_id"
            ),
            Employee(
                id = "lead_2",
                email = "michael.brown@mindmatrix.com",
                name = "Michael Brown",
                role = UserRole.LEAD,
                department = "Design",
                designation = "Creative Director",
                phone = "+1234567893",
                joinDate = "2023-02-10",
                managerId = "admin_id"
            ),
            Employee(
                id = "emp_1",
                email = "employee@mindmatrix.com",
                name = "John Doe",
                role = UserRole.EMPLOYEE,
                department = "Engineering",
                designation = "Software Engineer",
                phone = "+1234567892",
                joinDate = "2023-06-20",
                managerId = "lead_1"
            ),
            Employee(
                id = "emp_2",
                email = "jane.smith@mindmatrix.com",
                name = "Jane Smith",
                role = UserRole.EMPLOYEE,
                department = "Engineering",
                designation = "Frontend Developer",
                phone = "+1234567894",
                joinDate = "2023-08-05",
                managerId = "lead_1"
            ),
            Employee(
                id = "emp_3",
                email = "alex.wilson@mindmatrix.com",
                name = "Alex Wilson",
                role = UserRole.EMPLOYEE,
                department = "Design",
                designation = "UI/UX Designer",
                phone = "+1234567895",
                joinDate = "2023-09-12",
                managerId = "lead_2"
            ),
            Employee(
                id = "emp_4",
                email = "emily.davis@mindmatrix.com",
                name = "Emily Davis",
                role = UserRole.EMPLOYEE,
                department = "Marketing",
                designation = "Content Specialist",
                phone = "+1234567896",
                joinDate = "2023-11-30",
                managerId = "admin_id"
            ),
            Employee(
                id = "emp_5",
                email = "robert.lee@mindmatrix.com",
                name = "Robert Lee",
                role = UserRole.EMPLOYEE,
                department = "Engineering",
                designation = "Backend Engineer",
                phone = "+1234567897",
                joinDate = "2024-01-15",
                managerId = "lead_1"
            )
        )

        val seededList = mutableListOf<Employee>()
        employees.forEach { employee ->
            val existing = employeeRepository.getEmployeeByEmail(employee.email)
            if (existing == null) {
                employeeRepository.addEmployee(employee)
                seededList.add(employee)
            } else {
                val updated = employee.copy(id = existing.id)
                employeeRepository.updateEmployee(updated)
                seededList.add(updated)
            }
        }
        return seededList
    }
}
