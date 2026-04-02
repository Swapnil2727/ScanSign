package com.spatel.scansign.ui.scanner

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme

@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onScanComplete: () -> Unit,
    viewModel: ScannerViewModel,
) {
    val context = LocalContext.current
    var scannerError by remember { mutableStateOf<String?>(null) }

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setGalleryImportAllowed(true)
            .setPageLimit(20)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val launcher = rememberLauncherForActivityResult(
        contract = StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pdfUri = scanResult?.pdf?.uri
            val pageUris = scanResult?.pages?.mapNotNull { it.imageUri } ?: emptyList()
            if (pdfUri != null) {
                viewModel.onScanSuccess(pdfUri, pageUris)
                onScanComplete()
            } else {
                onBack()
            }
        } else {
            onBack()
        }
    }

    fun launchScanner(activity: ComponentActivity) {
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { error ->
                scannerError = error.message ?: "Scanner could not start"
            }
    }

    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity ?: run { onBack(); return@LaunchedEffect }
        launchScanner(activity)
    }

    if (scannerError != null) {
        ScannerErrorDialog(
            onRetry = {
                scannerError = null
                val activity = context as? ComponentActivity ?: run { onBack(); return@ScannerErrorDialog }
                launchScanner(activity)
            },
            onDismiss = onBack,
        )
    } else {
        ScannerLoadingContent()
    }
}

@Composable
private fun ScannerErrorDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scanner unavailable") },
        text = { Text("The document scanner could not start. Make sure Google Play Services is up to date and try again.") },
        confirmButton = { TextButton(onClick = onRetry) { Text("Try again") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ScannerLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@ThemePreviews
@Composable
private fun ScannerScreenPreview() {
    ScanSignTheme(dynamicColor = false) {
        ScannerLoadingContent()
    }
}

@ThemePreviews
@Composable
private fun ScannerErrorDialogPreview() {
    ScanSignTheme(dynamicColor = false) {
        ScannerErrorDialog(onRetry = {}, onDismiss = {})
    }
}
