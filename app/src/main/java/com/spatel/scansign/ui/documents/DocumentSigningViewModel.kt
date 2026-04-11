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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed interface SigningState {
    data object Idle    : SigningState
    data object Signing : SigningState
    data object Success : SigningState
    data class  Error(val message: String) : SigningState
}

data class SigningUiState(
    val pageIndex: Int = 0,
    val pageCount: Int = 1,
    val pageBitmap: Bitmap? = null,
    val signatures: List<Signature> = emptyList(),
    val selectedSignature: Signature? = null,
    val signingState: SigningState = SigningState.Idle,
)

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

    // ── Unified state (low-frequency: page metadata, signatures, op status) ───

    private val _uiState = MutableStateFlow(SigningUiState())
    val uiState: StateFlow<SigningUiState> = _uiState.asStateFlow()

    // ── High-frequency gesture state (updated 60fps during drag / resize) ─────

    private val _signatureOffset = MutableStateFlow(Offset.Zero)
    val signatureOffset: StateFlow<Offset> = _signatureOffset.asStateFlow()

    private val _signatureSize = MutableStateFlow(Size.Zero)
    val signatureSize: StateFlow<Size> = _signatureSize.asStateFlow()

    // ── Internal-only: PDF point dimensions needed for coordinate conversion ──

    private val _pageSizePt = MutableStateFlow<Pair<Int, Int>?>(null)

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        // Live signature list
        viewModelScope.launch {
            signatureRepository.getAll().collect { sigs ->
                _uiState.update { it.copy(signatures = sigs) }
            }
        }
        // Page count + first page render
        viewModelScope.launch {
            documentRepository.getById(documentId)
                .filterNotNull()
                .first()
                .let { doc -> _uiState.update { it.copy(pageCount = doc.pageCount.coerceAtLeast(1)) } }
            loadPage()
        }
    }

    // ── Page loading ──────────────────────────────────────────────────────────

    private fun loadPage() {
        val idx = _uiState.value.pageIndex
        viewModelScope.launch {
            val doc = documentRepository.getById(documentId).filterNotNull().first()
            val pdfFile = File(doc.pdfPath ?: return@launch)
            coroutineScope {
                val bmp  = async { pdfPageRenderer.renderPage(pdfFile, idx, RENDER_WIDTH_PX) }
                val size = async { pdfPageRenderer.getPageSizePt(pdfFile, idx) }
                bmp.await().onSuccess  { bitmap -> _uiState.update { it.copy(pageBitmap = bitmap) } }
                size.await().onSuccess { sizePt  -> _pageSizePt.value = sizePt }
            }
        }
    }

    // ── Page navigation ───────────────────────────────────────────────────────

    fun nextPage() {
        val state = _uiState.value
        if (state.pageIndex < state.pageCount - 1) {
            _uiState.update { it.copy(pageIndex = it.pageIndex + 1) }
            loadPage()
        }
    }

    fun prevPage() {
        val state = _uiState.value
        if (state.pageIndex > 0) {
            _uiState.update { it.copy(pageIndex = it.pageIndex - 1) }
            loadPage()
        }
    }

    // ── Signature selection ───────────────────────────────────────────────────

    fun selectSignature(sig: Signature) {
        _uiState.update { it.copy(selectedSignature = sig) }
        val bmp = _uiState.value.pageBitmap ?: return
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
        val bmp  = _uiState.value.pageBitmap ?: return
        val size = _signatureSize.value
        val cur  = _signatureOffset.value
        _signatureOffset.value = Offset(
            x = (cur.x + delta.x).coerceIn(0f, (bmp.width  - size.width).coerceAtLeast(0f)),
            y = (cur.y + delta.y).coerceIn(0f, (bmp.height - size.height).coerceAtLeast(0f)),
        )
    }

    // ── Resize ────────────────────────────────────────────────────────────────

    fun resizeSignature(delta: Offset) {
        val bmp = _uiState.value.pageBitmap ?: return
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
        val state  = _uiState.value
        val sig    = state.selectedSignature ?: return
        val bmp    = state.pageBitmap        ?: return
        val sizePt = _pageSizePt.value       ?: return
        val offset = _signatureOffset.value
        val sigSz  = _signatureSize.value

        viewModelScope.launch {
            _uiState.update { it.copy(signingState = SigningState.Signing) }
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
                    pageIndex       = state.pageIndex,
                    x               = offset.x     * scaleX,
                    y               = pdfY,
                    width           = sigSz.width  * scaleX,
                    height          = sigSz.height * scaleY,
                ).getOrThrow()
            }.fold(
                onSuccess = { _uiState.update { it.copy(signingState = SigningState.Success) } },
                onFailure = { err -> _uiState.update { it.copy(signingState = SigningState.Error(err.message ?: "Signing failed")) } },
            )
        }
    }

    fun clearSigningState() {
        _uiState.update { it.copy(signingState = SigningState.Idle) }
    }
}
