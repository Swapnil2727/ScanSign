package com.spatel.scansign.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.spatel.scansign.core.model.DocumentPage
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import com.spatel.scansign.util.shareDocument
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DocumentViewerScreen(
    initialPage: Int,
    onBack: () -> Unit,
    onSignClick: () -> Unit,
    viewModel: DocumentViewerViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val state = uiState) {
        is DocumentViewerUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DocumentViewerUiState.Success -> {
            DocumentViewerContent(
                title        = state.documentTitle,
                pages        = state.pages,
                initialPage  = initialPage,
                onBack       = onBack,
                onSignClick  = onSignClick,
                onShareClick = { shareDocument(context, state.pdfPath) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentViewerContent(
    title: String,
    pages: List<DocumentPage>,
    initialPage: Int,
    onBack: () -> Unit,
    onSignClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val pageCount      = pages.size.coerceAtLeast(1)
    val pagerState     = rememberPagerState(initialPage = initialPage.coerceIn(0, pageCount - 1)) { pageCount }
    val thumbnailState = rememberLazyListState()

    // Keep the thumbnail strip centred on the current page as the user swipes
    LaunchedEffect(pagerState.currentPage) {
        thumbnailState.animateScrollToItem(pagerState.currentPage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val displayTitle = if (title.length > 10) "${title.take(10)}…" else title
                    Text(
                        text     = displayTitle,
                        style    = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share document")
                    }
                    IconButton(onClick = onSignClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Sign document")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── 1. Page pager (fills remaining space) ─────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                PageView(page = pages.getOrNull(pageIndex))
            }

            // ── 2. Thumbnail strip ─────────────────────────────────────────
            LazyRow(
                state                 = thumbnailState,
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentPadding        = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(pages, key = { _, p -> p.id }) { idx, page ->
                    PageThumbnail(
                        page       = page,
                        pageIndex  = idx,
                        isSelected = idx == pagerState.currentPage,
                        pagerState = pagerState,
                    )
                }
            }

            // ── 3. AdMob banner slot ───────────────────────────────────────
            // TODO Week 12: replace with AdBanner(adUnitId = BuildConfig.VIEWER_BANNER_AD_UNIT_ID)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

// ── Individual page in pager ───────────────────────────────────────────────────

@Composable
private fun PageView(page: DocumentPage?) {
    if (page?.imagePath?.isNotEmpty() == true) {
        AsyncImage(
            model              = File(page.imagePath),
            contentDescription = "Page ${page.pageNumber + 1}",
            contentScale       = ContentScale.FillWidth,
            modifier           = Modifier.fillMaxWidth(),
        )
    } else {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

// ── Thumbnail in strip ─────────────────────────────────────────────────────────

@Composable
private fun PageThumbnail(
    page: DocumentPage,
    pageIndex: Int,
    isSelected: Boolean,
    pagerState: PagerState,
) {
    val scope       = rememberCoroutineScope()
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 68.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .clickable { scope.launch { pagerState.animateScrollToPage(pageIndex) } },
    ) {
        if (page.imagePath.isNotEmpty()) {
            AsyncImage(
                model              = File(page.imagePath),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        }
        // Page number badge
        Text(
            text     = "${pageIndex + 1}",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                )
                .padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewPages = listOf(
    DocumentPage("p1", "doc-1", 0, ""),
    DocumentPage("p2", "doc-1", 1, ""),
    DocumentPage("p3", "doc-1", 2, ""),
)

@ThemePreviews
@Composable
private fun DocumentViewerContentPreview() {
    ScanSignTheme {
        DocumentViewerContent(
            title        = "Invoice March 2025",
            pages        = previewPages,
            initialPage  = 0,
            onBack       = {},
            onSignClick  = {},
            onShareClick = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentViewerSinglePagePreview() {
    ScanSignTheme {
        DocumentViewerContent(
            title        = "Single Page Doc",
            pages        = listOf(DocumentPage("p1", "doc-1", 0, "")),
            initialPage  = 0,
            onBack       = {},
            onSignClick  = {},
            onShareClick = {},
        )
    }
}
