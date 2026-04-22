package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.mindmatrix.employeetracker.data.model.Document
import kotlinx.coroutines.flow.Flow

interface IDocumentRepository {
    fun getDocumentsForEmployee(employeeId: String): Flow<List<Document>>
    suspend fun uploadDocument(employeeId: String, fileUri: Uri, fileName: String, fileType: String): Result<Document>
    suspend fun deleteDocument(document: Document): Result<Unit>
}
