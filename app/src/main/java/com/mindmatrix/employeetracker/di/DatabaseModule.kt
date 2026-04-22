package com.mindmatrix.employeetracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindmatrix.employeetracker.data.local.AppDatabase
import com.mindmatrix.employeetracker.data.local.dao.AttendanceDao
import com.mindmatrix.employeetracker.data.local.dao.EmployeeDao
import com.mindmatrix.employeetracker.data.local.dao.PerformanceDao
import com.mindmatrix.employeetracker.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    fun provideEmployeeDao(database: AppDatabase): EmployeeDao {
        return database.employeeDao()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun providePerformanceDao(database: AppDatabase): PerformanceDao {
        return database.performanceDao()
    }

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }

    @Provides
    fun provideDepartmentDao(database: AppDatabase): com.mindmatrix.employeetracker.data.local.dao.DepartmentDao {
        return database.departmentDao()
    }
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE performance_reviews ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS employees_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    email TEXT NOT NULL,
                    name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    department TEXT NOT NULL,
                    designation TEXT NOT NULL,
                    contact TEXT NOT NULL,
                    joining_date TEXT NOT NULL,
                    profileImageUrl TEXT NOT NULL,
                    isActive INTEGER NOT NULL,
                    managerId TEXT NOT NULL,
                    badges TEXT NOT NULL,
                    lastUpdated INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO employees_new
                (id, email, name, role, department, designation, contact, joining_date, profileImageUrl, isActive, managerId, badges, lastUpdated)
                SELECT id, email, name, role, department, designation, phone, joinDate, profileImageUrl, isActive, managerId, badges, lastUpdated
                FROM employees
                """.trimIndent()
            )
            database.execSQL("DROP TABLE employees")
            database.execSQL("ALTER TABLE employees_new RENAME TO employees")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_employees_email ON employees(email)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_employees_department ON employees(department)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_employees_managerId ON employees(managerId)")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS tasks_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    employee_id TEXT NOT NULL,
                    assignedBy TEXT NOT NULL,
                    status TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    deadline TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    completedAt TEXT NOT NULL,
                    comments TEXT NOT NULL,
                    attachments TEXT NOT NULL,
                    isPersonalGoal INTEGER NOT NULL,
                    lastUpdated INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO tasks_new
                (id, title, description, employee_id, assignedBy, status, priority, deadline, createdAt, completedAt, comments, attachments, isPersonalGoal, lastUpdated)
                SELECT id, title, description, assignedTo, assignedBy, status, priority, dueDate, createdAt, completedAt, comments, attachments, isPersonalGoal, lastUpdated
                FROM tasks
                """.trimIndent()
            )
            database.execSQL("DROP TABLE tasks")
            database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_employee_id ON tasks(employee_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_status ON tasks(status)")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS performance_reviews_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    employee_id TEXT NOT NULL,
                    reviewerId TEXT NOT NULL,
                    date TEXT NOT NULL,
                    period TEXT NOT NULL,
                    quality_score INTEGER NOT NULL,
                    timeliness_score INTEGER NOT NULL,
                    attendance_score INTEGER NOT NULL,
                    communication_score INTEGER NOT NULL,
                    innovation_score INTEGER NOT NULL,
                    rawScore REAL NOT NULL,
                    overall_rating REAL NOT NULL,
                    status TEXT NOT NULL,
                    remarks TEXT NOT NULL,
                    goals TEXT NOT NULL,
                    strengths TEXT NOT NULL,
                    areasForImprovement TEXT NOT NULL,
                    lastUpdated INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO performance_reviews_new
                (id, employee_id, reviewerId, date, period, quality_score, timeliness_score, attendance_score, communication_score, innovation_score, rawScore, overall_rating, status, remarks, goals, strengths, areasForImprovement, lastUpdated)
                SELECT id, employeeId, reviewerId, reviewDate, period, qualityScore, productivityScore, attendanceScore, softSkillsScore, teamworkScore, rawScore, weightedScore, status, comments, goals, strengths, areasForImprovement, lastUpdated
                FROM performance_reviews
                """.trimIndent()
            )
            database.execSQL("DROP TABLE performance_reviews")
            database.execSQL("ALTER TABLE performance_reviews_new RENAME TO performance_reviews")
        }
    }

}
