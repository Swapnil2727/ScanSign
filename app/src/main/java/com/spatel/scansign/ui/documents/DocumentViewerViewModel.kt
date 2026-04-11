package com.spatel.scansign.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.model.DocumentPage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

sealed interface DocumentViewerUiState {
    data object Loading : DocumentViewerUiState
    data class Success(
        val documentTitle: String,
        val pages: List<DocumentPage>,
    ) : DocumentViewerUiState
}

class DocumentViewerViewModel(
    private val documentId: String,
    private val repository: DocumentRepository,
) : ViewModel() {

    val uiState: StateFlow<DocumentViewerUiState> = combine(
        repository.getById(documentId),
        flow { emit(repository.getPages(documentId)) },
    ) { document, pages ->
        if (document != null) {
            DocumentViewerUiState.Success(
                documentTitle = document.title,
                pages         = pages,
            )
        } else {
            DocumentViewerUiState.Loading
        }
    }
        .catch { emit(DocumentViewerUiState.Loading) }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = DocumentViewerUiState.Loading,
        )
}
