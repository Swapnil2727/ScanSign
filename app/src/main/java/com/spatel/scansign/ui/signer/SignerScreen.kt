package com.spatel.scansign.ui.signer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.model.SignatureType
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Public screen ─────────────────────────────────────────────────────────────

@Composable
fun SignerScreen(viewModel: SignerViewModel = koinViewModel()) {
    val selectedTab          by viewModel.selectedTab.collectAsStateWithLifecycle()
    val completedStrokes     by viewModel.completedStrokes.collectAsStateWithLifecycle()
    val currentStroke        by viewModel.currentStroke.collectAsStateWithLifecycle()
    val selectedImageUri     by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val savedSignatures      by viewModel.savedSignatures.collectAsStateWithLifecycle()
    val saveState            by viewModel.saveState.collectAsStateWithLifecycle()
    val transparentBg        by viewModel.transparentBackground.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SignerSaveState.Success ->
                snackbarHostState.showSnackbar("Signature saved")
            is SignerSaveState.Error ->
                snackbarHostState.showSnackbar((saveState as SignerSaveState.Error).message)
            else -> Unit
        }
        if (saveState !is SignerSaveState.Idle) viewModel.clearSaveState()
    }

    SignerContent(
        selectedTab = selectedTab,
        completedStrokes = completedStrokes,
        currentStroke = currentStroke,
        selectedImageUri = selectedImageUri,
        savedSignatures = savedSignatures,
        isSaving = saveState is SignerSaveState.Saving,
        snackbarHostState = snackbarHostState,
        onTabSelected = viewModel::selectTab,
        onStrokeStart = viewModel::startStroke,
        onStrokeMove = viewModel::continueStroke,
        onStrokeEnd = viewModel::endStroke,
        onUndo = viewModel::undoLastStroke,
        onClear = viewModel::clearDrawing,
        transparentBackground = transparentBg,
        onTransparentBackgroundChange = viewModel::setTransparentBackground,
        onSaveDrawn = { name, w, h -> viewModel.saveDrawnSignature(name, w, h, transparentBg) },
        onImageSelected = viewModel::onImageSelected,
        onSaveImage = viewModel::saveImageSignature,
        onDeleteSignature = viewModel::deleteSignature,
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignerContent(
    selectedTab: SignerTab,
    completedStrokes: List<List<Offset>>,
    currentStroke: List<Offset>,
    selectedImageUri: android.net.Uri?,
    savedSignatures: List<Signature>,
    isSaving: Boolean,
    snackbarHostState: SnackbarHostState,
    transparentBackground: Boolean,
    onTransparentBackgroundChange: (Boolean) -> Unit,
    onTabSelected: (SignerTab) -> Unit,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onSaveDrawn: (name: String, widthPx: Int, heightPx: Int) -> Unit,
    onImageSelected: (android.net.Uri) -> Unit,
    onSaveImage: (name: String) -> Unit,
    onDeleteSignature: (Signature) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Signatures") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val tabs = listOf(
                TabItem(SignerTab.DRAW, "Draw", Icons.Filled.Draw, Icons.Outlined.Draw),
                TabItem(SignerTab.IMAGE, "Image", Icons.Filled.Photo, Icons.Outlined.Photo),
            )
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.type,
                        onClick = { onTabSelected(tab.type) },
                        text = { Text(tab.label) },
                        icon = {
                            Icon(
                                if (selectedTab == tab.type) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            ) {
                item {
                    when (selectedTab) {
                        SignerTab.DRAW -> DrawTab(
                            completedStrokes = completedStrokes,
                            currentStroke = currentStroke,
                            isSaving = isSaving,
                            transparentBackground = transparentBackground,
                            onTransparentBackgroundChange = onTransparentBackgroundChange,
                            onStrokeStart = onStrokeStart,
                            onStrokeMove = onStrokeMove,
                            onStrokeEnd = onStrokeEnd,
                            onUndo = onUndo,
                            onClear = onClear,
                            onSave = onSaveDrawn,
                        )
                        SignerTab.IMAGE -> ImageTab(
                            selectedImageUri = selectedImageUri,
                            isSaving = isSaving,
                            onImageSelected = onImageSelected,
                            onSave = onSaveImage,
                        )
                    }
                }

                val filtered = savedSignatures.filter {
                    when (selectedTab) {
                        SignerTab.DRAW -> it.type == SignatureType.DRAWN
                        SignerTab.IMAGE -> it.type == SignatureType.IMAGE
                    }
                }
                if (filtered.isNotEmpty()) {
                    item {
                        Text(
                            "Saved",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        )
                    }
                    items(filtered, key = { it.id }) { sig ->
                        SavedSignatureItem(
                            signature = sig,
                            onDelete = { onDeleteSignature(sig) },
                        )
                    }
                }
            }
        }
    }
}

// ── Draw tab ──────────────────────────────────────────────────────────────────

@Composable
private fun DrawTab(
    completedStrokes: List<List<Offset>>,
    currentStroke: List<Offset>,
    isSaving: Boolean,
    transparentBackground: Boolean,
    onTransparentBackgroundChange: (Boolean) -> Unit,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onSave: (name: String, widthPx: Int, heightPx: Int) -> Unit,
) {
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val hasStrokes = completedStrokes.isNotEmpty() || currentStroke.isNotEmpty()

    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onStrokeStart(it) },
                        onDrag = { change, _ -> onStrokeMove(change.position) },
                        onDragEnd = onStrokeEnd,
                        onDragCancel = onStrokeEnd,
                    )
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeStyle = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                (completedStrokes + if (currentStroke.isNotEmpty()) listOf(currentStroke) else emptyList())
                    .forEach { stroke ->
                        drawPath(stroke.toComposePath(), color = Color.Black, style = strokeStyle)
                    }
            }
            if (!hasStrokes) {
                Text(
                    "Sign here",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Background option — affects how the signature is embedded on dark pages
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !transparentBackground,
                onClick  = { onTransparentBackgroundChange(false) },
                label    = { Text("White background") },
            )
            FilterChip(
                selected = transparentBackground,
                onClick  = { onTransparentBackgroundChange(true) },
                label    = { Text("No background") },
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = completedStrokes.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) { Text("Undo") }
            OutlinedButton(
                onClick = onClear,
                enabled = hasStrokes,
                modifier = Modifier.weight(1f),
            ) { Text("Clear") }
            Button(
                onClick = { showSaveDialog = true },
                enabled = completedStrokes.isNotEmpty() && !isSaving,
                modifier = Modifier.weight(1f),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }
    }

    if (showSaveDialog) {
        SaveSignatureDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                showSaveDialog = false
                onSave(name, canvasSize.width, canvasSize.height)
            },
        )
    }
}

private fun List<Offset>.toComposePath(): Path {
    val path = Path()
    if (isEmpty()) return path
    path.moveTo(first().x, first().y)
    if (size == 1) {
        path.lineTo(first().x + 0.01f, first().y + 0.01f)
        return path
    }
    for (i in 0 until size - 1) {
        val mid = Offset((this[i].x + this[i + 1].x) / 2f, (this[i].y + this[i + 1].y) / 2f)
        path.quadraticTo(this[i].x, this[i].y, mid.x, mid.y)
    }
    path.lineTo(last().x, last().y)
    return path
}

// ── Image tab ─────────────────────────────────────────────────────────────────

@Composable
private fun ImageTab(
    selectedImageUri: android.net.Uri?,
    isSaving: Boolean,
    onImageSelected: (android.net.Uri) -> Unit,
    onSave: (name: String) -> Unit,
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onImageSelected(uri) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected signature image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.weight(1f),
                ) { Text("Change") }
                Button(
                    onClick = { showSaveDialog = true },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("Save")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Photo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No image selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Photo, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Pick from gallery")
            }
        }
    }

    if (showSaveDialog) {
        SaveSignatureDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                showSaveDialog = false
                onSave(name)
            },
        )
    }
}

// ── Saved signature item ──────────────────────────────────────────────────────

@Composable
private fun SavedSignatureItem(signature: Signature, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = signature.bitmapPath?.let { File(it) },
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(signature.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${if (signature.type == SignatureType.DRAWN) "Drawn" else "Image"} · ${signature.createdAt.formatDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete signature?") },
            text = { Text("\"${signature.name}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

// ── Save name dialog ──────────────────────────────────────────────────────────

@Composable
private fun SaveSignatureDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name this signature") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g. My Signature") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private data class TabItem(
    val type: SignerTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private fun Long.formatDate(): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(this))

// ── Previews ──────────────────────────────────────────────────────────────────

@ThemePreviews
@Composable
private fun DrawTabPreview() {
    ScanSignTheme(dynamicColor = false) {
        DrawTab(
            completedStrokes = emptyList(),
            currentStroke = emptyList(),
            isSaving = false,
            transparentBackground = false,
            onTransparentBackgroundChange = {},
            onStrokeStart = {},
            onStrokeMove = {},
            onStrokeEnd = {},
            onUndo = {},
            onClear = {},
            onSave = { _, _, _ -> },
        )
    }
}

@ThemePreviews
@Composable
private fun ImageTabEmptyPreview() {
    ScanSignTheme(dynamicColor = false) {
        ImageTab(
            selectedImageUri = null,
            isSaving = false,
            onImageSelected = {},
            onSave = {},
        )
    }
}
