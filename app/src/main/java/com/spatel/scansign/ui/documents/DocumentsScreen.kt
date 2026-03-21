package com.spatel.scansign.ui.documents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Public screen (stateful) ──────────────────────────────────────────────────

@Composable
fun DocumentsScreen(
    onScanClick: () -> Unit = {},
    onSignClick: () -> Unit = {},
    onDocumentClick: (String) -> Unit = {},
    viewModel: DocumentsViewModel,
) {
    val documents by viewModel.documents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onDeleteRequest: (String) -> Unit = { id ->
        viewModel.requestDelete(id)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Document deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.undoDelete()
                SnackbarResult.Dismissed -> viewModel.confirmDelete()
            }
        }
    }

    DocumentsContent(
        documents = documents,
        searchQuery = searchQuery,
        isSearchActive = isSearchActive,
        snackbarHostState = snackbarHostState,
        onSearchToggle = {
            isSearchActive = !isSearchActive
            if (!isSearchActive) viewModel.onSearchQueryChange("")
        },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onScanClick = onScanClick,
        onSignClick = onSignClick,
        onDocumentClick = onDocumentClick,
        onDeleteRequest = onDeleteRequest,
    )
}

// ── Private content (stateless, previewable) ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentsContent(
    documents: List<Document>,
    searchQuery: String,
    isSearchActive: Boolean,
    snackbarHostState: SnackbarHostState,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onScanClick: () -> Unit,
    onSignClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScanSign", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSearchToggle) {
                        Icon(
                            if (isSearchActive) Icons.Outlined.Close else Icons.Outlined.Search,
                            contentDescription = if (isSearchActive) "Close search" else "Search",
                        )
                    }
                    IconButton(onClick = { }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "SP",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                if (!isSearchActive) {
                    item { GreetingHeader(documentCount = documents.size) }
                    item {
                        QuickActionsGrid(
                            onScanClick = onScanClick,
                            onSignClick = onSignClick,
                        )
                    }
                }
                item {
                    if (isSearchActive) SearchResultsHeader(documents.size)
                    else RecentDocumentsHeader()
                }
                if (documents.isEmpty()) {
                    item {
                        if (isSearchActive && searchQuery.isNotBlank()) {
                            NoSearchResultsHint(searchQuery)
                        } else {
                            EmptyDocumentsHint()
                        }
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        SwipeableDocumentItem(
                            doc = doc,
                            onDocumentClick = onDocumentClick,
                            onDeleteRequest = onDeleteRequest,
                        )
                    }
                }
            }
        }
    }
}

// ── Swipeable document item ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDocumentItem(
    doc: Document,
    onDocumentClick: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest(doc.id)
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { SwipeDeleteBackground(dismissState.targetValue) },
    ) {
        DocumentListItem(doc = doc, onClick = { onDocumentClick(doc.id) })
    }
}

@Composable
private fun SwipeDeleteBackground(targetValue: SwipeToDismissBoxValue) {
    val color by animateColorAsState(
        targetValue = if (targetValue == SwipeToDismissBoxValue.EndToStart)
            MaterialTheme.colorScheme.errorContainer
        else
            Color.Transparent,
        label = "swipeBg",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            Icons.Filled.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(end = 20.dp),
        )
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search documents…") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(documentCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            "Good morning,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Saumil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "$documentCount document${if (documentCount == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickActionsGrid(
    onScanClick: () -> Unit,
    onSignClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CameraAlt,
                title = "Scan",
                subtitle = "Document",
                containerColor = Color(0xFF1565C0),
                onClick = onScanClick,
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Draw,
                title = "Sign",
                subtitle = "Document",
                containerColor = Color(0xFF00897B),
                onClick = onSignClick,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Folder,
                title = "My",
                subtitle = "Documents",
                containerColor = Color(0xFFE65100),
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.FileOpen,
                title = "Import",
                subtitle = "PDF & Photos",
                containerColor = Color(0xFF6A1B9A),
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.heightIn(min = 100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SearchResultsHeader(resultCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "$resultCount found",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecentDocumentsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Recent Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = { }) { Text("View All") }
    }
}

@Composable
private fun EmptyDocumentsHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No documents yet. Tap Scan to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoSearchResultsHint(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No results for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DocumentListItem(doc: Document, onClick: () -> Unit) {
    val statusColor = doc.status.color()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (doc.thumbnailPath != null) {
                    AsyncImage(
                        model = File(doc.thumbnailPath!!),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    doc.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${doc.createdAt.formatDate()} · ${doc.pageCount} page${if (doc.pageCount == 1) "" else "s"} · ${doc.fileSize.formatSize()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.12f),
            ) {
                Text(
                    doc.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun DocumentStatus.color(): Color = when (this) {
    DocumentStatus.SIGNED -> Color(0xFF43A047)
    DocumentStatus.DRAFT -> Color(0xFFFFA000)
    DocumentStatus.PENDING -> Color(0xFF1565C0)
    DocumentStatus.SCANNED -> Color(0xFF00897B)
}

private fun Long.formatDate(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))

private fun Long.formatSize(): String = when {
    this < 1024 -> "$this B"
    this < 1024 * 1024 -> "${"%.0f".format(this / 1024.0)} KB"
    else -> "${"%.1f".format(this / (1024.0 * 1024))} MB"
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewDocuments = listOf(
    Document(
        id = "1",
        title = "Invoice March 2025",
        createdAt = 1_740_000_000_000L,
        updatedAt = 1_740_000_000_000L,
        pageCount = 3,
        fileSize = 1_240_000L,
        status = DocumentStatus.SIGNED,
        thumbnailPath = null,
    ),
    Document(
        id = "2",
        title = "NDA — Acme Corp",
        createdAt = 1_739_000_000_000L,
        updatedAt = 1_739_000_000_000L,
        pageCount = 5,
        fileSize = 2_048_000L,
        status = DocumentStatus.SCANNED,
        thumbnailPath = null,
    ),
    Document(
        id = "3",
        title = "Expense Report Q1",
        createdAt = 1_738_000_000_000L,
        updatedAt = 1_738_000_000_000L,
        pageCount = 1,
        fileSize = 512_000L,
        status = DocumentStatus.DRAFT,
        thumbnailPath = null,
    ),
)

// ── Previews ──────────────────────────────────────────────────────────────────

@ThemePreviews
@Composable
private fun DocumentsWithDocumentsPreview() {
    ScanSignTheme {
        DocumentsContent(
            documents = previewDocuments,
            searchQuery = "",
            isSearchActive = false,
            snackbarHostState = remember { SnackbarHostState() },
            onSearchToggle = {},
            onSearchQueryChange = {},
            onScanClick = {},
            onSignClick = {},
            onDocumentClick = {},
            onDeleteRequest = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentsEmptyPreview() {
    ScanSignTheme {
        DocumentsContent(
            documents = emptyList(),
            searchQuery = "",
            isSearchActive = false,
            snackbarHostState = remember { SnackbarHostState() },
            onSearchToggle = {},
            onSearchQueryChange = {},
            onScanClick = {},
            onSignClick = {},
            onDocumentClick = {},
            onDeleteRequest = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentsSearchActivePreview() {
    ScanSignTheme {
        DocumentsContent(
            documents = previewDocuments.take(1),
            searchQuery = "Invoice",
            isSearchActive = true,
            snackbarHostState = remember { SnackbarHostState() },
            onSearchToggle = {},
            onSearchQueryChange = {},
            onScanClick = {},
            onSignClick = {},
            onDocumentClick = {},
            onDeleteRequest = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentsSearchNoResultsPreview() {
    ScanSignTheme {
        DocumentsContent(
            documents = emptyList(),
            searchQuery = "receipt",
            isSearchActive = true,
            snackbarHostState = remember { SnackbarHostState() },
            onSearchToggle = {},
            onSearchQueryChange = {},
            onScanClick = {},
            onSignClick = {},
            onDocumentClick = {},
            onDeleteRequest = {},
        )
    }
}
