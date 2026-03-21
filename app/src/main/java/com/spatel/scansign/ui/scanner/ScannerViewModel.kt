package com.spatel.scansign.ui.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel : ViewModel() {
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    fun onScanSuccess(pdfUri: Uri, pageUris: List<Uri>) {
        _scanResult.value = ScanResult(pdfUri = pdfUri, pageUris = pageUris)
    }

    fun clearScanResult() {
        _scanResult.value = null
    }
}

data class ScanResult(val pdfUri: Uri, val pageUris: List<Uri>)
