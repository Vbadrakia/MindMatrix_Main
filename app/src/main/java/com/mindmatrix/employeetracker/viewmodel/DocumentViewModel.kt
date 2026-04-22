package com.mindmatrix.employeetracker.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.employeetracker.data.model.Document
import com.mindmatrix.employeetracker.data.repository.IDocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: IDocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentState())
    val state: StateFlow<DocumentState> = _state.asStateFlow()

    fun loadDocuments(employeeId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getDocumentsForEmployee(employeeId).collectLatest { docs ->
                _state.value = _state.value.copy(documents = docs, isLoading = false)
            }
        }
    }

    fun uploadDocument(employeeId: String, uri: Uri, name: String, type: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.uploadDocument(employeeId, uri, name, type).onSuccess {
                _state.value = _state.value.copy(isLoading = false, error = null)
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document).onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
