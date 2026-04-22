package com.mindmatrix.employeetracker.di

import android.content.Context
import androidx.room.Room
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
        ).build()
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
}
