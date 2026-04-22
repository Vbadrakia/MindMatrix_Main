package com.mindmatrix.employeetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService(
    private val firestore: FirebaseFirestore
) {
    suspend fun getEmployees() =
        firestore.collection("employees").get().await()
}