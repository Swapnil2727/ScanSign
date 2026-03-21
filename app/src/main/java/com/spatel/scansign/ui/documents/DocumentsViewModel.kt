package com.spatel.scansign.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.DeleteDocumentUseCase
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.model.Document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentsViewModel(
    repository: DocumentRepository,
    private val deleteDocument: DeleteDocumentUseCase,
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    // IDs swiped away but not yet confirmed deleted (pending undo window)
    private val _pendingDeleteIds = MutableStateFlow<Set<String>>(emptySet())
    private var lastDeletedId: String? = null

    val documents: StateFlow<List<Document>> = combine(
        repository.getAll(),
        searchQuery,
        _pendingDeleteIds,
    ) { docs, query, pendingIds ->
        docs
            .filter { it.id !in pendingIds }
            .let { filtered ->
                if (query.isBlank()) filtered
                else filtered.filter { it.title.contains(query, ignoreCase = true) }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    /** Called on swipe — item disappears from the list immediately. */
    fun requestDelete(id: String) {
        lastDeletedId = id
        _pendingDeleteIds.update { it + id }
    }

    /** Called when snackbar times out or is dismissed without Undo — permanently deletes. */
    fun confirmDelete() {
        val id = lastDeletedId ?: return
        _pendingDeleteIds.update { it - id }
        lastDeletedId = null
        viewModelScope.launch { deleteDocument(id) }
    }

    /** Called when user taps Undo — item reappears in the list. */
    fun undoDelete() {
        val id = lastDeletedId ?: return
        _pendingDeleteIds.update { it - id }
        lastDeletedId = null
    }
}
