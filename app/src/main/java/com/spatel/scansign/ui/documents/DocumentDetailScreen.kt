package com.spatel.scansign.ui.documents

import android.content.Context
import android.content.Intent
import com.spatel.scansign.util.shareDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentPage
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Public screen — connects ViewModel ────────────────────────────────────────

@Composable
fun DocumentDetailScreen(
    onBack: () -> Unit,
    onDocumentDeleted: () -> Unit,
    onSignClick: () -> Unit,
    onPageClick: (pageIndex: Int) -> Unit,
    viewModel: DocumentDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                DocumentDetailEvent.Deleted -> onDocumentDeleted()
            }
        }
    }

    DocumentDetailContent(
        uiState = uiState,
        onBack = onBack,
        onShare = { pdfPath -> shareDocument(context, pdfPath) },
        onRename = viewModel::rename,
        onDelete = viewModel::delete,
        onSignClick = onSignClick,
        onPageClick = onPageClick,
    )
}

// ── Private content — pure and previewable ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentDetailContent(
    uiState: DocumentDetailUiState,
    onBack: () -> Unit,
    onShare: (pdfPath: String) -> Unit,
    onRename: (newTitle: String) -> Unit,
    onDelete: () -> Unit,
    onSignClick: () -> Unit,
    onPageClick: (pageIndex: Int) -> Unit,
) {
    val title = if (uiState is DocumentDetailUiState.Success) uiState.document.title else ""
    var showRenameSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is DocumentDetailUiState.Success) {
                        IconButton(onClick = { showRenameSheet = true }) {
                            Icon(
                                Icons.Filled.DriveFileRenameOutline,
                                contentDescription = "Rename",
                            )
                        }
                        uiState.document.pdfPath?.let { path ->
                            IconButton(onClick = { onShare(path) }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share")
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (uiState is DocumentDetailUiState.Success) {
                FloatingActionButton(onClick = onSignClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Sign document")
                }
            }
        },
    ) { innerPadding ->
        when (uiState) {
            DocumentDetailUiState.Loading -> LoadingState(Modifier.padding(innerPadding))
            is DocumentDetailUiState.Error -> ErrorState(uiState.message, Modifier.padding(innerPadding))
            is DocumentDetailUiState.Success -> DocumentBody(
                document = uiState.document,
                pages = uiState.pages,
                onPageClick = onPageClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (showRenameSheet && uiState is DocumentDetailUiState.Success) {
        RenameBottomSheet(
            currentTitle = uiState.document.title,
            onDismiss = { showRenameSheet = false },
            onConfirm = { newTitle ->
                onRename(newTitle)
                showRenameSheet = false
            },
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
        )
    }
}

// ── Body ──────────────────────────────────────────────────────────────────────

@Composable
private fun DocumentBody(
    document: Document,
    pages: List<DocumentPage>,
    onPageClick: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { MetadataCard(document) }
        item {
            Text(
                text = "Pages (${pages.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
        if (pages.isEmpty()) {
            item { PagesFallback(document) }
        } else {
            itemsIndexed(pages, key = { _, page -> page.id }) { _, page ->
                PageItem(page, onClick = { onPageClick(page.pageNumber) })
            }
        }
    }
}

@Composable
private fun MetadataCard(document: Document) {
    val statusColor = document.status.color()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = document.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            MetadataRow("Pages", "${document.pageCount}")
            Spacer(modifier = Modifier.height(8.dp))
            MetadataRow("Size", document.fileSize.formatSize())
            Spacer(modifier = Modifier.height(8.dp))
            MetadataRow("Created", document.createdAt.formatDate())
            Spacer(modifier = Modifier.height(8.dp))
            MetadataRow("Modified", document.updatedAt.formatDate())
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PageItem(page: DocumentPage, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column {
            AsyncImage(
                model = File(page.imagePath),
                contentDescription = "Page ${page.pageNumber + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            )
            Text(
                text = "Page ${page.pageNumber + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

// Fallback when pages list is empty but PDF path exists (e.g. older scan without page files)
@Composable
private fun PagesFallback(document: Document) {
    document.pdfPath?.let { path ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) {
            document.thumbnailPath?.let { thumbPath ->
                AsyncImage(
                    model = File(thumbPath),
                    contentDescription = "Document preview",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
        }
    }
}

// ── States ────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

// ── Rename bottom sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameBottomSheet(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember(currentTitle) { mutableStateOf(currentTitle) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Rename document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) onConfirm(text.trim())
                }),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                    enabled = text.isNotBlank(),
                ) {
                    Text("Rename")
                }
            }
        }
    }
}

// ── Delete confirm dialog ─────────────────────────────────────────────────────

@Composable
private fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete document?") },
        text = { Text("This will permanently delete the document and all its pages.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun DocumentStatus.color(): Color = when (this) {
    DocumentStatus.SIGNED  -> Color(0xFF43A047)
    DocumentStatus.DRAFT   -> Color(0xFFFFA000)
    DocumentStatus.PENDING -> Color(0xFF1565C0)
    DocumentStatus.SCANNED -> Color(0xFF00897B)
}

private fun Long.formatDate(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))

private fun Long.formatSize(): String = when {
    this < 1024        -> "$this B"
    this < 1024 * 1024 -> "${"%.0f".format(this / 1024.0)} KB"
    else               -> "${"%.1f".format(this / (1024.0 * 1024))} MB"
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewDocument = Document(
    id = "preview-1",
    title = "Invoice March 2025",
    createdAt = 1_700_000_000_000L,
    updatedAt = 1_700_000_000_000L,
    pageCount = 3,
    fileSize = 1_240_000L,
    status = DocumentStatus.SCANNED,
)

private val previewPages = listOf(
    DocumentPage("p1", "preview-1", 0, ""),
    DocumentPage("p2", "preview-1", 1, ""),
    DocumentPage("p3", "preview-1", 2, ""),
)

@ThemePreviews
@Composable
private fun DocumentDetailContentPreview() {
    ScanSignTheme {
        DocumentDetailContent(
            uiState = DocumentDetailUiState.Success(previewDocument, previewPages),
            onBack = {},
            onShare = {},
            onRename = {},
            onDelete = {},
            onSignClick = {},
            onPageClick = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentDetailLoadingPreview() {
    ScanSignTheme {
        DocumentDetailContent(
            uiState = DocumentDetailUiState.Loading,
            onBack = {},
            onShare = {},
            onRename = {},
            onDelete = {},
            onSignClick = {},
            onPageClick = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MetadataCardPreview() {
    ScanSignTheme {
        MetadataCard(document = previewDocument)
    }
}

@ThemePreviews
@Composable
private fun RenameBottomSheetPreview() {
    ScanSignTheme {
        RenameBottomSheet(
            currentTitle = "Invoice March 2025",
            onDismiss = {},
            onConfirm = {},
        )
    }
}
