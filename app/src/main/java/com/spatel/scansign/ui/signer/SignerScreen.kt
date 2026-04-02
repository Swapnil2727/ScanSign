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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableIntStateOf
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
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val completedStrokes by viewModel.completedStrokes.collectAsStateWithLifecycle()
    val currentStroke by viewModel.currentStroke.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val savedSignatures by viewModel.savedSignatures.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val isGeneratingKey by viewModel.isGeneratingKey.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on success or error
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
        isGeneratingKey = isGeneratingKey,
        snackbarHostState = snackbarHostState,
        onTabSelected = viewModel::selectTab,
        onStrokeStart = viewModel::startStroke,
        onStrokeMove = viewModel::continueStroke,
        onStrokeEnd = viewModel::endStroke,
        onUndo = viewModel::undoLastStroke,
        onClear = viewModel::clearDrawing,
        onSaveDrawn = { name, w, h -> viewModel.saveDrawnSignature(name, w, h) },
        onImageSelected = viewModel::onImageSelected,
        onSaveImage = viewModel::saveImageSignature,
        onCreateDigital = viewModel::createDigitalSignature,
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
    isGeneratingKey: Boolean,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (SignerTab) -> Unit,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onSaveDrawn: (name: String, widthPx: Int, heightPx: Int) -> Unit,
    onImageSelected: (android.net.Uri) -> Unit,
    onSaveImage: (name: String) -> Unit,
    onCreateDigital: (name: String) -> Unit,
    onDeleteSignature: (Signature) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Signatures") })
        },
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
                TabItem(SignerTab.DIGITAL, "Digital", Icons.Filled.Key, Icons.Outlined.Key),
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
                        SignerTab.DIGITAL -> DigitalTab(
                            isGeneratingKey = isGeneratingKey,
                            onCreateKey = onCreateDigital,
                        )
                    }
                }

                // Saved signatures section — filtered by current tab's type
                val filtered = savedSignatures.filter {
                    when (selectedTab) {
                        SignerTab.DRAW -> it.type == SignatureType.DRAWN
                        SignerTab.IMAGE -> it.type == SignatureType.IMAGE
                        SignerTab.DIGITAL -> it.type == SignatureType.DIGITAL
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
        // Drawing canvas
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
                val strokeStyle = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
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

        Spacer(Modifier.height(12.dp))

        // Stroke action row
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
        path.lineTo(first().x + 0.01f, first().y + 0.01f) // force paint for single tap
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

// ── Digital tab ───────────────────────────────────────────────────────────────

@Composable
private fun DigitalTab(
    isGeneratingKey: Boolean,
    onCreateKey: (name: String) -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Digital signatures",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Creates an RSA-2048 key pair stored in Android Keystore. " +
                        "The private key never leaves your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(
            onClick = { showCreateDialog = true },
            enabled = !isGeneratingKey,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isGeneratingKey) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Generating…")
            } else {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create digital signature")
            }
        }
    }

    if (showCreateDialog) {
        SaveSignatureDialog(
            title = "New digital signature",
            placeholder = "e.g. Work, Personal",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                showCreateDialog = false
                onCreateKey(name)
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
            // Thumbnail for drawn/image signatures
            if (signature.bitmapPath != null) {
                AsyncImage(
                    model = File(signature.bitmapPath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    signature.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    when (signature.type) {
                        SignatureType.DRAWN -> "Drawn · ${signature.createdAt.formatDate()}"
                        SignatureType.IMAGE -> "Image · ${signature.createdAt.formatDate()}"
                        SignatureType.DIGITAL -> "RSA-2048 · ${signature.createdAt.formatDate()}"
                    },
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
    title: String = "Name this signature",
    placeholder: String = "e.g. My Signature",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(placeholder) },
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

@ThemePreviews
@Composable
private fun DigitalTabPreview() {
    ScanSignTheme(dynamicColor = false) {
        DigitalTab(isGeneratingKey = false, onCreateKey = {})
    }
}
