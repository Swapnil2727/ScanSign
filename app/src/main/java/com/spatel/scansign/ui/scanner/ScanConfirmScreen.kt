package com.spatel.scansign.ui.scanner

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme

@Composable
fun ScanConfirmScreen(
    onDiscard: () -> Unit,
    onSaveConfirmed: () -> Unit,
    viewModel: ScannerViewModel,
) {
    val scanResult by viewModel.scanResult.collectAsState()

    // Only guard against arriving with no result (e.g. process death).
    // Using Unit key so this does NOT re-fire when clearScanResult() is called on Save.
    LaunchedEffect(Unit) {
        if (scanResult == null) onDiscard()
    }

    ScanConfirmContent(
        pageUris = scanResult?.pageUris ?: emptyList(),
        onDiscard = onDiscard,
        onSaveConfirmed = {
            viewModel.clearScanResult()
            onSaveConfirmed()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanConfirmContent(
    pageUris: List<Uri>,
    onDiscard: () -> Unit,
    onSaveConfirmed: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Scan") },
                navigationIcon = {
                    IconButton(onClick = onDiscard) {
                        Icon(Icons.Filled.Close, contentDescription = "Discard")
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Discard")
                    }
                    Button(
                        onClick = onSaveConfirmed,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save")
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp),
        ) {
            Text(
                text = "${pageUris.size} pages scanned",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                items(pageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(width = 120.dp, height = 160.dp),
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ScanConfirmContentPreview() {
    ScanSignTheme(dynamicColor = false) {
        ScanConfirmContent(
            pageUris = emptyList(),
            onDiscard = {},
            onSaveConfirmed = {},
        )
    }
}
