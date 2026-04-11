package com.spatel.scansign.ui.documents

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.model.SignatureType
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme
import java.io.File

@Composable
fun DocumentSigningScreen(
    onBack: () -> Unit,
    onSigned: () -> Unit,
    onNavigateToSigner: () -> Unit,
    viewModel: DocumentSigningViewModel,
) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val signatureOffset by viewModel.signatureOffset.collectAsStateWithLifecycle()
    val signatureSize   by viewModel.signatureSize.collectAsStateWithLifecycle()
    val signatures      = uiState.signatures
    val selectedSig     = uiState.selectedSignature
    val pageIndex       = uiState.pageIndex
    val pageCount       = uiState.pageCount
    val pageBitmap      = uiState.pageBitmap
    val signingState    = uiState.signingState

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(signingState) {
        when (val s = signingState) {
            is SigningState.Success -> onSigned()
            is SigningState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.clearSigningState()
            }

            else -> Unit
        }
    }

    DocumentSigningContent(
        pageBitmap = pageBitmap,
        pageIndex = pageIndex,
        pageCount = pageCount,
        signatures = signatures,
        selectedSig = selectedSig,
        signatureOffset = signatureOffset,
        signatureSize = signatureSize,
        signingState = signingState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSelectSig = viewModel::selectSignature,
        onDrag = viewModel::dragSignature,
        onResize = viewModel::resizeSignature,
        onNextPage = viewModel::nextPage,
        onPrevPage = viewModel::prevPage,
        onConfirm = viewModel::confirm,
        onNavigateToSigner = onNavigateToSigner,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentSigningContent(
    pageBitmap: Bitmap?,
    pageIndex: Int,
    pageCount: Int,
    signatures: List<Signature>,
    selectedSig: Signature?,
    signatureOffset: Offset,
    signatureSize: Size,
    signingState: SigningState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSelectSig: (Signature) -> Unit,
    onDrag: (Offset) -> Unit,
    onResize: (Offset) -> Unit,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onConfirm: () -> Unit,
    onNavigateToSigner: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place Signature") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── 1. PDF page + drag overlay ─────────────────────────────────
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val density = LocalDensity.current
                val displayWidthPx = with(density) { maxWidth.roundToPx() }
                val scale = displayWidthPx.toFloat() / DocumentSigningViewModel.RENDER_WIDTH_PX

                if (pageBitmap != null) {
                    Image(
                        bitmap = pageBitmap.asImageBitmap(),
                        contentDescription = "Page ${pageIndex + 1}",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (selectedSig != null) {
                        SignatureDragOverlay(
                            signature = selectedSig,
                            offset = signatureOffset,
                            size = signatureSize,
                            scale = scale,
                            onDrag = onDrag,
                            onResize = onResize,
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }

            // ── 2. Page navigation ─────────────────────────────────────────
            PageNavigationRow(
                pageIndex = pageIndex,
                pageCount = pageCount,
                onPrevPage = onPrevPage,
                onNextPage = onNextPage,
            )

            // ── 3. Signature picker ────────────────────────────────────────
            SignaturePickerRow(
                signatures = signatures,
                selectedSig = selectedSig,
                onSelect = onSelectSig,
                onNavigateToSigner = onNavigateToSigner,
            )

            // ── 4. Apply button ────────────────────────────────────────────
            Button(
                onClick = onConfirm,
                enabled = selectedSig != null && signingState !is SigningState.Signing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (signingState is SigningState.Signing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Apply Signature")
            }
        }
    }
}

// ── Signature drag overlay ─────────────────────────────────────────────────────

@Composable
private fun SignatureDragOverlay(
    signature: Signature,
    offset: Offset,
    size: Size,
    scale: Float,           // displayPx per bitmapPx
    onDrag: (Offset) -> Unit,
    onResize: (Offset) -> Unit,
) {
    val density = LocalDensity.current
    val offsetDp = with(density) {
        DpOffset((offset.x * scale).toDp(), (offset.y * scale).toDp())
    }
    val widthDp = with(density) { (size.width * scale).toDp() }
    val heightDp = with(density) { (size.height * scale).toDp() }

    Box(
        modifier = Modifier
            .offset(offsetDp.x, offsetDp.y)
            .size(widthDp, heightDp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
            )
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    // Convert display-pixel delta → bitmap-pixel delta
                    onDrag(Offset(dragAmount.x / scale, dragAmount.y / scale))
                }
            },
    ) {
        signature.bitmapPath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = "Signature preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.85f),
            )
        }

        // ── Resize handle (bottom-right corner) ───────────────────────────
        Box(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        onResize(Offset(dragAmount.x / scale, dragAmount.y / scale))
                    }
                },
        )
    }
}

// ── Signature picker row ───────────────────────────────────────────────────────

@Composable
private fun SignaturePickerRow(
    signatures: List<Signature>,
    selectedSig: Signature?,
    onSelect: (Signature) -> Unit,
    onNavigateToSigner: () -> Unit,
) {
    Text(
        text = "Choose signature",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
    )
    if (signatures.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Icon - Increased size for better visual anchoring
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Draw,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Text Content - Takes remaining space
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No signatures yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Create your first one to sign documents faster.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                // 3. The Action - Uses a Tonal Button with an Arrow
                FilledTonalButton(
                    onClick = onNavigateToSigner,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            items(signatures, key = { it.id }) { sig ->
                SignatureThumbnail(
                    signature = sig,
                    isSelected = sig.id == selectedSig?.id,
                    onClick = { onSelect(sig) },
                )
            }
        }
    }
}

@Composable
private fun SignatureThumbnail(
    signature: Signature,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        onClick = onClick,
        modifier = Modifier
            .size(width = 96.dp, height = 56.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
    ) {
        signature.bitmapPath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = signature.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

// ── Page navigation row ────────────────────────────────────────────────────────

@Composable
private fun PageNavigationRow(
    pageIndex: Int,
    pageCount: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onPrevPage,
            enabled = pageIndex > 0,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous page")
        }
        Text(
            text = "Page ${pageIndex + 1} / $pageCount",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        IconButton(
            onClick = onNextPage,
            enabled = pageIndex < pageCount - 1,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next page")
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewSignatures = listOf(
    Signature("s1", "My Signature", 0L, SignatureType.DRAWN, bitmapPath = null),
    Signature("s2", "Initials", 0L, SignatureType.IMAGE, bitmapPath = null),
)

@ThemePreviews
@Composable
private fun DocumentSigningNoSignaturesPreview() {
    ScanSignTheme {
        DocumentSigningContent(
            pageBitmap = null,
            pageIndex = 0,
            pageCount = 3,
            signatures = emptyList(),
            selectedSig = null,
            signatureOffset = Offset.Zero,
            signatureSize = Size.Zero,
            signingState = SigningState.Idle,
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onSelectSig = {},
            onDrag = {},
            onResize = {},
            onNextPage = {},
            onPrevPage = {},
            onConfirm = {},
            onNavigateToSigner = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentSigningWithSignaturesPreview() {
    ScanSignTheme {
        DocumentSigningContent(
            pageBitmap = null,
            pageIndex = 0,
            pageCount = 3,
            signatures = previewSignatures,
            selectedSig = previewSignatures.first(),
            signatureOffset = Offset(100f, 200f),
            signatureSize = Size(270f, 90f),
            signingState = SigningState.Idle,
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onSelectSig = {},
            onDrag = {},
            onResize = {},
            onNextPage = {},
            onPrevPage = {},
            onConfirm = {},
            onNavigateToSigner = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DocumentSigningInProgressPreview() {
    ScanSignTheme {
        DocumentSigningContent(
            pageBitmap = null,
            pageIndex = 1,
            pageCount = 3,
            signatures = previewSignatures,
            selectedSig = previewSignatures.first(),
            signatureOffset = Offset(100f, 200f),
            signatureSize = Size(270f, 90f),
            signingState = SigningState.Signing,
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onSelectSig = {},
            onDrag = {},
            onResize = {},
            onNextPage = {},
            onPrevPage = {},
            onConfirm = {},
            onNavigateToSigner = {},
        )
    }
}
