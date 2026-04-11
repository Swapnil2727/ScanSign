package com.spatel.scansign.ui.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.SaveScannedDocumentUseCase
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.pdf.ImagesToPdfConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val scanResult: ScanResult? = null,
    val saveState: SaveState = SaveState.Idle,
)

class ScannerViewModel(
    private val saveDocument: SaveScannedDocumentUseCase,
    private val imagesToPdfConverter: ImagesToPdfConverter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onScanSuccess(pdfUri: Uri, pageUris: List<Uri>) {
        _uiState.update { it.copy(scanResult = ScanResult(pdfUri = pdfUri, pageUris = pageUris)) }
    }

    fun save(title: String) {
        val result = _uiState.value.scanResult ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(saveState = SaveState.Saving) }
            val outcome = saveDocument(result.resolvedPdfUri(), result.pageUris, title)
            _uiState.update {
                it.copy(
                    saveState = outcome.fold(
                        onSuccess = { doc -> SaveState.Success(doc) },
                        onFailure = { err -> SaveState.Error(err.message ?: "Save failed") },
                    ),
                )
            }
        }
    }

    fun onGalleryImagesSelected(imageUris: List<Uri>) {
        _uiState.update { it.copy(scanResult = null, saveState = SaveState.Idle) }
        viewModelScope.launch {
            imagesToPdfConverter.convert(imageUris).fold(
                onSuccess = { pdfFile ->
                    _uiState.update {
                        it.copy(scanResult = ScanResult(pdfFile = pdfFile, pageUris = imageUris))
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(saveState = SaveState.Error(err.message ?: "Import failed"))
                    }
                },
            )
        }
    }

    fun clearScanResult() {
        _uiState.update { it.copy(scanResult = null, saveState = SaveState.Idle) }
    }
}

data class ScanResult(
    val pdfUri: Uri? = null,
    val pdfFile: java.io.File? = null,
    val pageUris: List<Uri>,
) {
    fun resolvedPdfUri(): Uri = pdfUri ?: Uri.fromFile(pdfFile!!)
}

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data class Success(val document: Document) : SaveState
    data class Error(val message: String) : SaveState
}
