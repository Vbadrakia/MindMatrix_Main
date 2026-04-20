package com.mindmatrix.employeetracker.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindmatrix.employeetracker.data.repository.IAttendanceRepository
import com.mindmatrix.employeetracker.data.repository.IEmployeeRepository
import com.mindmatrix.employeetracker.data.repository.IPerformanceRepository
import com.mindmatrix.employeetracker.data.repository.ITaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that handles periodic data synchronization.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val employeeRepository: IEmployeeRepository,
    private val taskRepository: ITaskRepository,
    private val attendanceRepository: IAttendanceRepository,
    private val performanceRepository: IPerformanceRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "employee_tracker_sync"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting periodic sync...")
            
            val employeeSyncResult = employeeRepository.syncEmployees()
            val taskSyncResult = taskRepository.syncTasks()
            val attendanceSyncResult = attendanceRepository.syncAttendance()
            val performanceSyncResult = performanceRepository.syncPerformanceData()
            
            val results = listOf(
                employeeSyncResult,
                taskSyncResult,
                attendanceSyncResult,
                performanceSyncResult
            )

            if (results.all { it.isSuccess }) {
                Log.d(TAG, "Sync completed successfully")
                Result.success()
            } else {
                Log.e(TAG, "Sync failed for some repositories")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}", e)
            Result.retry()
        }
    }
}
