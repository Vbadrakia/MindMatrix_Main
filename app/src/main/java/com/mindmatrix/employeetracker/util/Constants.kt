package com.mindmatrix.employeetracker.util

/**
 * Application-wide constants.
 */
object Constants {
    // Firestore collection names
    const val COLLECTION_EMPLOYEES = "employees"
    const val COLLECTION_TASKS = "tasks"
    const val COLLECTION_ATTENDANCE = "attendance"
    const val COLLECTION_PERFORMANCE = "performance_reviews"

    // Default values
    const val DEFAULT_PAGE_SIZE = 20
    const val OFFICE_START_HOUR = 9
    const val LATE_THRESHOLD_HOUR = 10

    // Date formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val TIME_FORMAT = "HH:mm"
    const val DISPLAY_DATE_FORMAT = "MMM dd, yyyy"
}
