package com.mindmatrix.employeetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.util.SampleDataGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sampleDataGenerator: SampleDataGenerator
) : ViewModel() {

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    private val _seedSuccess = MutableStateFlow<Boolean?>(null)
    val seedSuccess: StateFlow<Boolean?> = _seedSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun seedData() {
        viewModelScope.launch {
            _isSeeding.value = true
            _error.value = null
            _seedSuccess.value = null
            try {
                sampleDataGenerator.seedLastMonthData()
                _seedSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to seed data"
                _seedSuccess.value = false
            } finally {
                _isSeeding.value = false
            }
        }
    }

    fun clearStatus() {
        _seedSuccess.value = null
        _error.value = null
    }
}
