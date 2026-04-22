package com.mindmatrix.employeetracker.data.local.dao

import androidx.room.*
import com.mindmatrix.employeetracker.data.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE ownerId = :employeeId")
    fun getDocumentsForEmployee(employeeId: String): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents WHERE ownerId = :employeeId")
    suspend fun deleteDocumentsForEmployee(employeeId: String)
}
