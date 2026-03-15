package com.spatel.scansign.ui.signer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme

// Placeholder — replaced in Week 8 with draw/image/digital signature tabs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignerScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign") }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Signature manager — coming in Week 8", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@ThemePreviews
@Composable
private fun SignerScreenPreview() {
    ScanSignTheme(dynamicColor = false) { SignerScreen() }
}
