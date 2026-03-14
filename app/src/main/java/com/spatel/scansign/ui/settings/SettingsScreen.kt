package com.spatel.scansign.ui.settings

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

// Placeholder — replaced in Week 9 with theme, storage and app-info prefs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Settings — coming in Week 9", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@ThemePreviews
@Composable
private fun SettingsScreenPreview() {
    ScanSignTheme(dynamicColor = false) { SettingsScreen() }
}
