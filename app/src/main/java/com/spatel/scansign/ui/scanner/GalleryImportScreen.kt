package com.spatel.scansign.ui.scanner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme

@Composable
fun GalleryImportScreen(
    onBack: () -> Unit,
    onImportComplete: () -> Unit,
    viewModel: ScannerViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scanResult = uiState.scanResult
    val saveState  = uiState.saveState

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onGalleryImagesSelected(uris)
        } else {
            onBack()
        }
    }

    // Navigate forward once image-to-PDF conversion completes
    LaunchedEffect(scanResult) {
        if (scanResult != null) onImportComplete()
    }

    // Launch picker only if we don't already have a pending result
    LaunchedEffect(Unit) {
        if (viewModel.uiState.value.scanResult == null) {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    if (saveState is SaveState.Error) {
        GalleryImportErrorDialog(
            message = (saveState as SaveState.Error).message,
            onDismiss = {
                viewModel.clearScanResult()
                onBack()
            },
        )
    } else {
        GalleryImportLoadingContent()
    }
}

@Composable
private fun GalleryImportErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import failed") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
    )
}

@Composable
private fun GalleryImportLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Importing images…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun GalleryImportLoadingPreview() {
    ScanSignTheme(dynamicColor = false) {
        GalleryImportLoadingContent()
    }
}

@ThemePreviews
@Composable
private fun GalleryImportErrorDialogPreview() {
    ScanSignTheme(dynamicColor = false) {
        GalleryImportErrorDialog(
            message = "Failed to convert images: out of memory",
            onDismiss = {},
        )
    }
}
