package com.mindmatrix.employeetracker.di

import com.mindmatrix.employeetracker.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEmployeeRepository(
        employeeRepository: EmployeeRepository
    ): IEmployeeRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepository: TaskRepository
    ): ITaskRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        attendanceRepository: AttendanceRepository
    ): IAttendanceRepository

    @Binds
    @Singleton
    abstract fun bindPerformanceRepository(
        performanceRepository: PerformanceRepository
    ): IPerformanceRepository
}
