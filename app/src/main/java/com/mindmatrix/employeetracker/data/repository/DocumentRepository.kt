package com.mindmatrix.employeetracker.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mindmatrix.employeetracker.data.model.Document
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IDocumentRepository {

    private val collection = firestore.collection("documents")
    private val storageRef = storage.reference.child("documents")

    override fun getDocumentsForEmployee(employeeId: String): Flow<List<Document>> = callbackFlow {
        val listener = collection.whereEqualTo("ownerId", employeeId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val docs = snapshot?.documents?.map { doc ->
                Document.fromMap(doc.id, doc.data ?: emptyMap())
            }.orEmpty()
            trySend(docs)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun uploadDocument(
        employeeId: String,
        fileUri: Uri,
        fileName: String,
        fileType: String
    ): Result<Document> = try {
        val docId = UUID.randomUUID().toString()
        val ref = storageRef.child("$employeeId/$docId")
        ref.putFile(fileUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        val document = Document(
            id = docId,
            name = fileName,
            url = downloadUrl,
            ownerId = employeeId,
            type = fileType,
            uploadDate = LocalDate.now().toString()
        )

        collection.document(docId).set(document.toMap()).await()
        Result.success(document)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDocument(document: Document): Result<Unit> = try {
        storage.getReferenceFromUrl(document.url).delete().await()
        collection.document(document.id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
