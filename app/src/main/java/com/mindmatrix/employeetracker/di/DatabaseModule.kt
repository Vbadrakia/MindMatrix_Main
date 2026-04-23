package com.mindmatrix.employeetracker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * DEPRECATED: This module is no longer used as the project has migrated to Firestore.
 */
@Module
@InstallIn(SingletonComponent::class)
object LegacyDatabaseModule {
    // No-op
}
