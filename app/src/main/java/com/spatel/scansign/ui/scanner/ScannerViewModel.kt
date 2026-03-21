package com.spatel.scansign.ui.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.SaveScannedDocumentUseCase
import com.spatel.scansign.core.model.Document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val saveDocument: SaveScannedDocumentUseCase,
) : ViewModel() {

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun onScanSuccess(pdfUri: Uri, pageUris: List<Uri>) {
        _scanResult.value = ScanResult(pdfUri = pdfUri, pageUris = pageUris)
    }

    fun save(title: String) {
        val result = _scanResult.value ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val outcome = saveDocument(result.pdfUri, result.pageUris, title)
            _saveState.value = outcome.fold(
                onSuccess = { SaveState.Success(it) },
                onFailure = { SaveState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
        _saveState.value = SaveState.Idle
    }
}

data class ScanResult(val pdfUri: Uri, val pageUris: List<Uri>)

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data class Success(val document: Document) : SaveState
    data class Error(val message: String) : SaveState
}
