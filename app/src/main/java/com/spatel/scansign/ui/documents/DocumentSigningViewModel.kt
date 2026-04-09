package com.spatel.scansign.ui.documents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.data.SignDocumentUseCase
import com.spatel.scansign.core.data.SignatureRepository
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.pdf.PdfPageRenderer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface SigningState {
    data object Idle    : SigningState
    data object Signing : SigningState
    data object Success : SigningState
    data class  Error(val message: String) : SigningState
}

class DocumentSigningViewModel(
    private val documentId: String,
    private val documentRepository: DocumentRepository,
    private val signatureRepository: SignatureRepository,
    private val pdfPageRenderer: PdfPageRenderer,
    private val signDocumentUseCase: SignDocumentUseCase,
    @Suppress("UNUSED_PARAMETER") context: Context,
) : ViewModel() {

    companion object {
        const val RENDER_WIDTH_PX = 1080
    }

    // ── Saved signatures (live from DB) ───────────────────────────────────────

    val signatures: StateFlow<List<Signature>> = signatureRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Selected signature ────────────────────────────────────────────────────

    private val _selectedSignature = MutableStateFlow<Signature?>(null)
    val selectedSignature: StateFlow<Signature?> = _selectedSignature.asStateFlow()

    // ── Page state ────────────────────────────────────────────────────────────

    private val _pageIndex = MutableStateFlow(0)
    val pageIndex: StateFlow<Int> = _pageIndex.asStateFlow()

    private var pageCount: Int = 1

    private val _pageBitmap = MutableStateFlow<Bitmap?>(null)
    val pageBitmap: StateFlow<Bitmap?> = _pageBitmap.asStateFlow()

    private val _pageSizePt = MutableStateFlow<Pair<Int, Int>?>(null)
    val pageSizePt: StateFlow<Pair<Int, Int>?> = _pageSizePt.asStateFlow()

    // ── Drag state (bitmap-pixel space) ───────────────────────────────────────

    private val _signatureOffset = MutableStateFlow(Offset.Zero)
    val signatureOffset: StateFlow<Offset> = _signatureOffset.asStateFlow()

    private val _signatureSize = MutableStateFlow(Size.Zero)
    val signatureSize: StateFlow<Size> = _signatureSize.asStateFlow()

    // ── Signing operation state ───────────────────────────────────────────────

    private val _signingState = MutableStateFlow<SigningState>(SigningState.Idle)
    val signingState: StateFlow<SigningState> = _signingState.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            documentRepository.getById(documentId)
                .filterNotNull()
                .first()
                .let { pageCount = it.pageCount.coerceAtLeast(1) }
            loadPage()
        }
    }

    // ── Page loading ──────────────────────────────────────────────────────────

    private fun loadPage() {
        val idx = _pageIndex.value
        viewModelScope.launch {
            val doc = documentRepository.getById(documentId).filterNotNull().first()
            val pdfFile = File(doc.pdfPath ?: return@launch)
            coroutineScope {
                val bmp  = async { pdfPageRenderer.renderPage(pdfFile, idx, RENDER_WIDTH_PX) }
                val size = async { pdfPageRenderer.getPageSizePt(pdfFile, idx) }
                bmp.await().onSuccess  { _pageBitmap.value = it }
                size.await().onSuccess { _pageSizePt.value = it }
            }
        }
    }

    // ── Page navigation ───────────────────────────────────────────────────────

    fun nextPage() {
        if (_pageIndex.value < pageCount - 1) {
            _pageIndex.value++
            loadPage()
        }
    }

    fun prevPage() {
        if (_pageIndex.value > 0) {
            _pageIndex.value--
            loadPage()
        }
    }

    // ── Signature selection ───────────────────────────────────────────────────

    fun selectSignature(sig: Signature) {
        _selectedSignature.value = sig
        val bmp = _pageBitmap.value ?: return
        val w = bmp.width * 0.25f
        val h = w / 3f
        _signatureSize.value   = Size(w, h)
        _signatureOffset.value = Offset(
            x = (bmp.width  - w) / 2f,
            y = (bmp.height - h) / 2f,
        )
    }

    // ── Drag ──────────────────────────────────────────────────────────────────

    fun dragSignature(delta: Offset) {
        val bmp  = _pageBitmap.value ?: return
        val size = _signatureSize.value
        val cur  = _signatureOffset.value
        _signatureOffset.value = Offset(
            x = (cur.x + delta.x).coerceIn(0f, (bmp.width  - size.width).coerceAtLeast(0f)),
            y = (cur.y + delta.y).coerceIn(0f, (bmp.height - size.height).coerceAtLeast(0f)),
        )
    }

    // ── Resize ────────────────────────────────────────────────────────────────

    fun resizeSignature(delta: Offset) {
        val bmp = _pageBitmap.value ?: return
        val cur = _signatureSize.value
        val off = _signatureOffset.value
        val minPx = bmp.width * 0.05f          // 5 % of page width minimum
        _signatureSize.value = Size(
            width  = (cur.width  + delta.x).coerceIn(minPx, (bmp.width  - off.x).coerceAtLeast(minPx)),
            height = (cur.height + delta.y).coerceIn(minPx, (bmp.height - off.y).coerceAtLeast(minPx)),
        )
    }

    // ── Confirm / sign ────────────────────────────────────────────────────────

    fun confirm() {
        val sig    = _selectedSignature.value ?: return
        val bmp    = _pageBitmap.value        ?: return
        val sizePt = _pageSizePt.value        ?: return
        val offset = _signatureOffset.value
        val sigSz  = _signatureSize.value

        viewModelScope.launch {
            _signingState.value = SigningState.Signing
            runCatching {
                val sigBitmap: Bitmap =
                    BitmapFactory.decodeFile(sig.bitmapPath ?: error("Signature '${sig.name}' has no bitmap path"))
                        ?: error("Failed to decode bitmap for '${sig.name}'")
                val (widthPt, heightPt) = sizePt
                val scaleX = widthPt.toFloat()  / bmp.width
                val scaleY = heightPt.toFloat() / bmp.height
                // PDF Y-axis is bottom-left origin; flip from bitmap top-left origin
                val pdfY = heightPt - (offset.y + sigSz.height) * scaleY

                signDocumentUseCase(
                    documentId      = documentId,
                    signatureBitmap = sigBitmap,
                    pageIndex       = _pageIndex.value,
                    x               = offset.x      * scaleX,
                    y               = pdfY,
                    width           = sigSz.width   * scaleX,
                    height          = sigSz.height  * scaleY,
                ).getOrThrow()
            }.fold(
                onSuccess = { _signingState.value = SigningState.Success },
                onFailure = { _signingState.value = SigningState.Error(it.message ?: "Signing failed") },
            )
        }
    }

    fun clearSigningState() {
        _signingState.value = SigningState.Idle
    }
}
