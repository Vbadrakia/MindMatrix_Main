package com.mindmatrix.employeetracker.di

import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.employeetracker.data.remote.FirebaseService
import com.mindmatrix.employeetracker.data.repository.EmployeeRepositoryImpl
import com.mindmatrix.employeetracker.domain.repository.EmployeeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseService(firestore: FirebaseFirestore): FirebaseService {
        return FirebaseService(firestore)
    }

    @Provides
    @Singleton
    fun provideEmployeeRepository(firebaseService: FirebaseService): EmployeeRepository {
        return EmployeeRepositoryImpl(firebaseService)
    }
}
