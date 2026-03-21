package com.spatel.scansign.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentPage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DocumentDetailUiState {
    data object Loading : DocumentDetailUiState
    data class Success(
        val document: Document,
        val pages: List<DocumentPage>,
    ) : DocumentDetailUiState
    data class Error(val message: String) : DocumentDetailUiState
}

sealed interface DocumentDetailEvent {
    data object Deleted : DocumentDetailEvent
}

class DocumentDetailViewModel(
    private val documentId: String,
    private val repository: DocumentRepository,
) : ViewModel() {

    private val _events = MutableSharedFlow<DocumentDetailEvent>()
    val events: SharedFlow<DocumentDetailEvent> = _events.asSharedFlow()

    val uiState: StateFlow<DocumentDetailUiState> = combine(
        repository.getById(documentId),
        flow { emit(repository.getPages(documentId)) },
    ) { document, pages ->
        if (document != null) {
            DocumentDetailUiState.Success(document, pages)
        } else {
            DocumentDetailUiState.Error("Document not found")
        }
    }
        .catch { emit(DocumentDetailUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DocumentDetailUiState.Loading,
        )

    fun rename(newTitle: String) {
        viewModelScope.launch {
            repository.rename(documentId, newTitle)
            // getById Flow auto-re-emits with updated title
        }
    }

    fun delete() {
        viewModelScope.launch {
            repository.delete(documentId)
            _events.emit(DocumentDetailEvent.Deleted)
        }
    }
}
