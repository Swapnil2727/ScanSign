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
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val saveDocument: SaveScannedDocumentUseCase,
    private val imagesToPdfConverter: ImagesToPdfConverter,
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
            val outcome = saveDocument(result.resolvedPdfUri(), result.pageUris, title)
            _saveState.value = outcome.fold(
                onSuccess = { SaveState.Success(it) },
                onFailure = { SaveState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun onGalleryImagesSelected(imageUris: List<Uri>) {
        _scanResult.value = null
        _saveState.value = SaveState.Idle
        viewModelScope.launch {
            imagesToPdfConverter.convert(imageUris).fold(
                onSuccess = { pdfFile ->
                    _scanResult.value = ScanResult(
                        pdfFile = pdfFile,
                        pageUris = imageUris,
                    )
                },
                onFailure = {
                    _saveState.value = SaveState.Error(it.message ?: "Import failed")
                },
            )
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
        _saveState.value = SaveState.Idle
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
