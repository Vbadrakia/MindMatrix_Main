package com.mindmatrix.employeetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.model.AttendanceRecord
import com.mindmatrix.employeetracker.data.model.AttendanceStatus
import com.mindmatrix.employeetracker.data.model.StatusCount
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IAttendanceRepository {
    private val collection = firestore.collection("attendance")

    override fun getAttendanceForEmployee(employeeId: String): Flow<List<AttendanceRecord>> = callbackFlow {
        var primaryRecords: List<AttendanceRecord> = emptyList()
        var legacyRecords: List<AttendanceRecord> = emptyList()

        val listener = collection
            .whereEqualTo("employee_id", employeeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                primaryRecords = snapshot?.documents?.map { doc ->
                    AttendanceRecord.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend((primaryRecords + legacyRecords).distinctBy { it.id })
            }

        val legacyListener = collection
            .whereEqualTo("employeeId", employeeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                legacyRecords = snapshot?.documents?.map { doc ->
                    AttendanceRecord.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend((primaryRecords + legacyRecords).distinctBy { it.id })
            }
        awaitClose {
            listener.remove()
            legacyListener.remove()
        }
    }

    override fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> = callbackFlow {
        val listener = collection
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents?.map { doc ->
                    AttendanceRecord.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(records)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllAttendance(): Flow<List<AttendanceRecord>> = callbackFlow {
        val listener = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents?.map { doc ->
                    AttendanceRecord.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(records)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun markAttendance(record: AttendanceRecord): Result<String> = try {
        val docRef = collection.add(record.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateAttendance(record: AttendanceRecord): Result<Unit> = try {
        collection.document(record.id).set(record.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAttendanceSummary(employeeId: String): List<StatusCount> = try {
        val snapshot = collection
            .whereEqualTo("employee_id", employeeId)
            .get()
            .await()
        val legacySnapshot = collection.whereEqualTo("employeeId", employeeId).get().await()
        val records = (snapshot.documents + legacySnapshot.documents).map { doc ->
            AttendanceRecord.fromMap(doc.id, doc.data ?: emptyMap())
        }.distinctBy { it.id }
        AttendanceStatus.entries.map { status ->
            StatusCount(
                status = status,
                count = records.count { it.status == status }
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun getTodayAttendanceForEmployee(employeeId: String, date: String): AttendanceRecord? = try {
        val snapshot = collection
            .whereEqualTo("employee_id", employeeId)
            .whereEqualTo("date", date)
            .get()
            .await()
        (snapshot.documents + collection
            .whereEqualTo("employeeId", employeeId)
            .whereEqualTo("date", date)
            .get()
            .await()
            .documents)
            .map { doc -> AttendanceRecord.fromMap(doc.id, doc.data ?: emptyMap()) }
            .firstOrNull()
    } catch (e: Exception) {
        null
    }

    override suspend fun syncAttendance(): Result<Unit> {
        // Mock sync logic
        return Result.success(Unit)
    }
}
