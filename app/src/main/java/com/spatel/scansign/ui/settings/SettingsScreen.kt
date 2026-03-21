package com.spatel.scansign.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spatel.scansign.core.datastore.AppTheme
import com.spatel.scansign.core.datastore.ScanQuality
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import org.koin.compose.viewmodel.koinViewModel

// ── Public screen (stateful) ──────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        onThemeChange = viewModel::setAppTheme,
        onScanQualityChange = viewModel::setScanQuality,
    )
}

// ── Private content (stateless, previewable) ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onThemeChange: (AppTheme) -> Unit,
    onScanQualityChange: (ScanQuality) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is SettingsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding),
                ) {
                    AppearanceSection(
                        appTheme = uiState.appTheme,
                        onThemeChange = onThemeChange,
                    )
                    SectionDivider()
                    ScanningSection(
                        scanQuality = uiState.scanQuality,
                        onScanQualityChange = onScanQualityChange,
                    )
                    SectionDivider()
                    StorageSection(
                        documentCount = uiState.documentCount,
                        totalStorageBytes = uiState.totalStorageBytes,
                    )
                    SectionDivider()
                    AboutSection()
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Sections ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSection(
    appTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
) {
    SectionHeader(icon = Icons.Outlined.Palette, title = "Appearance")
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SettingLabel(
            label = "Theme",
            description = "Controls the app colour scheme",
        )
        Spacer(modifier = Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AppTheme.entries.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = appTheme == theme,
                    onClick = { onThemeChange(theme) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = AppTheme.entries.size,
                    ),
                    label = {
                        Text(
                            when (theme) {
                                AppTheme.SYSTEM -> "System"
                                AppTheme.LIGHT  -> "Light"
                                AppTheme.DARK   -> "Dark"
                            },
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ScanningSection(
    scanQuality: ScanQuality,
    onScanQualityChange: (ScanQuality) -> Unit,
) {
    SectionHeader(icon = Icons.Outlined.PhotoCamera, title = "Scanning")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingLabel(
            modifier = Modifier.weight(1f),
            label = "High quality",
            description = "Larger files, better for fine print",
        )
        Switch(
            checked = scanQuality == ScanQuality.HIGH,
            onCheckedChange = { isHigh ->
                onScanQualityChange(if (isHigh) ScanQuality.HIGH else ScanQuality.STANDARD)
            },
        )
    }
}

@Composable
private fun StorageSection(
    documentCount: Int,
    totalStorageBytes: Long,
) {
    SectionHeader(icon = Icons.Outlined.Folder, title = "Storage")
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        StorageRow(label = "Documents", value = "$documentCount")
        Spacer(modifier = Modifier.height(8.dp))
        StorageRow(label = "Total size", value = totalStorageBytes.formatSize())
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrDefault("—")
    }
    SectionHeader(icon = Icons.Outlined.Info, title = "About")
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        StorageRow(label = "Version", value = versionName ?: "—")
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SettingLabel(
    modifier: Modifier = Modifier,
    label: String,
    description: String,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun Long.formatSize(): String = when {
    this < 1024L          -> "$this B"
    this < 1024L * 1024   -> "${"%.0f".format(this / 1024.0)} KB"
    else                  -> "${"%.1f".format(this / (1024.0 * 1024))} MB"
}

// ── Previews ──────────────────────────────────────────────────────────────────

@ThemePreviews
@Composable
private fun SettingsPreview() {
    ScanSignTheme(dynamicColor = false) {
        SettingsContent(
            uiState = SettingsUiState.Success(
                appTheme = AppTheme.SYSTEM,
                scanQuality = ScanQuality.STANDARD,
                documentCount = 12,
                totalStorageBytes = 18_432_000L,
            ),
            onThemeChange = {},
            onScanQualityChange = {},
        )
    }
}

@ThemePreviews
@Composable
private fun SettingsHighQualityDarkPreview() {
    ScanSignTheme(darkTheme = true, dynamicColor = false) {
        SettingsContent(
            uiState = SettingsUiState.Success(
                appTheme = AppTheme.DARK,
                scanQuality = ScanQuality.HIGH,
                documentCount = 3,
                totalStorageBytes = 4_096_000L,
            ),
            onThemeChange = {},
            onScanQualityChange = {},
        )
    }
}
