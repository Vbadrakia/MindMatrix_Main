package com.mindmatrix.employeetracker.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Custom hook to provide haptic feedback triggers.
 */
@Composable
fun rememberHapticFeedbackHandler(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

/**
 * Triggers a light vibration for successful actions.
 */
@Composable
fun rememberSuccessHapticFeedback(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
