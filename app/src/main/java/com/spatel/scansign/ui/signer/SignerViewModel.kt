package com.spatel.scansign.ui.signer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.SignatureRepository
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.model.SignatureType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class SignerTab { DRAW, IMAGE }

sealed interface SignerSaveState {
    data object Idle : SignerSaveState
    data object Saving : SignerSaveState
    data class Success(val signature: Signature) : SignerSaveState
    data class Error(val message: String) : SignerSaveState
}

class SignerViewModel(
    private val signatureRepository: SignatureRepository,
    private val context: Context,
) : ViewModel() {

    // ── Tab ──────────────────────────────────────────────────────────────────

    private val _selectedTab = MutableStateFlow(SignerTab.DRAW)
    val selectedTab: StateFlow<SignerTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: SignerTab) {
        _selectedTab.value = tab
    }

    // ── Saved signatures (live from DB) ───────────────────────────────────────

    val savedSignatures: StateFlow<List<Signature>> = signatureRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Draw tab ─────────────────────────────────────────────────────────────

    private val _completedStrokes = MutableStateFlow<List<List<Offset>>>(emptyList())
    val completedStrokes: StateFlow<List<List<Offset>>> = _completedStrokes.asStateFlow()

    private val _currentStroke = MutableStateFlow<List<Offset>>(emptyList())
    val currentStroke: StateFlow<List<Offset>> = _currentStroke.asStateFlow()

    val hasDrawing: Boolean get() = _completedStrokes.value.isNotEmpty()

    fun startStroke(offset: Offset) {
        _currentStroke.value = listOf(offset)
    }

    fun continueStroke(offset: Offset) {
        _currentStroke.update { it + offset }
    }

    fun endStroke() {
        val stroke = _currentStroke.value
        if (stroke.isNotEmpty()) {
            _completedStrokes.update { it + listOf(stroke) }
        }
        _currentStroke.value = emptyList()
    }

    fun undoLastStroke() {
        _completedStrokes.update { strokes -> strokes.dropLast(1) }
    }

    fun clearDrawing() {
        _completedStrokes.value = emptyList()
        _currentStroke.value = emptyList()
    }

    // ── Background option (Draw tab) ──────────────────────────────────────────

    private val _transparentBackground = MutableStateFlow(false)
    val transparentBackground: StateFlow<Boolean> = _transparentBackground.asStateFlow()

    fun setTransparentBackground(value: Boolean) {
        _transparentBackground.value = value
    }

    // ── Image tab ─────────────────────────────────────────────────────────────

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _selectedImageUri.value = uri
    }

    // ── Save state ────────────────────────────────────────────────────────────

    private val _saveState = MutableStateFlow<SignerSaveState>(SignerSaveState.Idle)
    val saveState: StateFlow<SignerSaveState> = _saveState.asStateFlow()

    fun clearSaveState() {
        _saveState.value = SignerSaveState.Idle
    }

    // ── Save drawn signature ──────────────────────────────────────────────────

    fun saveDrawnSignature(name: String, canvasWidthPx: Int, canvasHeightPx: Int, transparentBackground: Boolean = false) {
        if (name.isBlank() || _completedStrokes.value.isEmpty()) return
        viewModelScope.launch {
            _saveState.value = SignerSaveState.Saving
            runCatching {
                val bitmap = renderStrokesToBitmap(
                    strokes = _completedStrokes.value,
                    widthPx = canvasWidthPx,
                    heightPx = canvasHeightPx,
                    transparentBackground = transparentBackground,
                )
                val file = saveBitmapToFile(bitmap)
                val signature = Signature(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    type = SignatureType.DRAWN,
                    bitmapPath = file.absolutePath,
                    createdAt = System.currentTimeMillis(),
                )
                signatureRepository.save(signature).getOrThrow()
                clearDrawing()
                signature
            }.fold(
                onSuccess = { _saveState.value = SignerSaveState.Success(it) },
                onFailure = { _saveState.value = SignerSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }

    // ── Save image signature ──────────────────────────────────────────────────

    fun saveImageSignature(name: String) {
        val uri = _selectedImageUri.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            _saveState.value = SignerSaveState.Saving
            runCatching {
                val destFile = newSignatureFile()
                context.contentResolver.openInputStream(uri)!!.use { input ->
                    destFile.outputStream().use { input.copyTo(it) }
                }
                val signature = Signature(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    type = SignatureType.IMAGE,
                    bitmapPath = destFile.absolutePath,
                    createdAt = System.currentTimeMillis(),
                )
                signatureRepository.save(signature).getOrThrow()
                _selectedImageUri.value = null
                signature
            }.fold(
                onSuccess = { _saveState.value = SignerSaveState.Success(it) },
                onFailure = { _saveState.value = SignerSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun deleteSignature(signature: Signature) {
        viewModelScope.launch {
            signature.bitmapPath?.let { runCatching { File(it).delete() } }
            signatureRepository.delete(signature.id)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun newSignatureFile(): File {
        val dir = File(context.filesDir, "signatures").also { it.mkdirs() }
        return File(dir, "${UUID.randomUUID()}.png")
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = newSignatureFile()
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    companion object {
        private const val STROKE_WIDTH_PX = 6f

        /**
         * Renders completed strokes to an [android.graphics.Bitmap].
         *
         * @param transparentBackground when `true`, the canvas background is left
         *   transparent (ARGB_8888 default). When `false` (default), fills with white.
         *   Use `true` to avoid a white box on dark document pages.
         */
        fun renderStrokesToBitmap(
            strokes: List<List<Offset>>,
            widthPx: Int,
            heightPx: Int,
            transparentBackground: Boolean = false,
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            if (!transparentBackground) canvas.drawColor(Color.WHITE)
            val paint = Paint().apply {
                color = Color.BLACK
                strokeWidth = STROKE_WIDTH_PX
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            strokes.forEach { stroke -> canvas.drawPath(stroke.toAndroidPath(), paint) }
            return bitmap
        }

        private fun List<Offset>.toAndroidPath(): Path {
            val path = Path()
            if (isEmpty()) return path
            path.moveTo(first().x, first().y)
            if (size == 1) {
                path.addCircle(first().x, first().y, STROKE_WIDTH_PX / 2f, Path.Direction.CW)
                return path
            }
            for (i in 0 until size - 1) {
                val mid = Offset((this[i].x + this[i + 1].x) / 2f, (this[i].y + this[i + 1].y) / 2f)
                path.quadTo(this[i].x, this[i].y, mid.x, mid.y)
            }
            path.lineTo(last().x, last().y)
            return path
        }
    }
}
