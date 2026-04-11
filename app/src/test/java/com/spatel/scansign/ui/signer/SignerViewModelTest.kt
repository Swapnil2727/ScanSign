package com.spatel.scansign.ui.signer

import androidx.compose.ui.geometry.Offset
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.model.SignatureType
import com.spatel.scansign.util.FakeSignatureRepository
import com.spatel.scansign.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.mockk
import android.content.Context

@OptIn(ExperimentalCoroutinesApi::class)
class SignerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepo = FakeSignatureRepository()

    private lateinit var viewModel: SignerViewModel

    @Before
    fun setup() {
        // Context is only used for file I/O in save flows — not exercised in these pure-logic tests
        viewModel = SignerViewModel(fakeRepo, mockk<Context>(relaxed = true))
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    @Test
    fun `initial tab is DRAW`() = runTest {
        assertEquals(SignerTab.DRAW, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `selectTab updates selected tab`() = runTest {
        viewModel.selectTab(SignerTab.IMAGE)
        assertEquals(SignerTab.IMAGE, viewModel.uiState.value.selectedTab)
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Test
    fun `startStroke initialises current stroke`() {
        viewModel.startStroke(Offset(10f, 20f))
        assertEquals(listOf(Offset(10f, 20f)), viewModel.currentStroke.value)
    }

    @Test
    fun `continueStroke appends points`() {
        viewModel.startStroke(Offset(0f, 0f))
        viewModel.continueStroke(Offset(5f, 5f))
        viewModel.continueStroke(Offset(10f, 10f))
        assertEquals(3, viewModel.currentStroke.value.size)
    }

    @Test
    fun `endStroke moves current stroke to completedStrokes and clears current`() {
        viewModel.startStroke(Offset(0f, 0f))
        viewModel.continueStroke(Offset(10f, 10f))
        viewModel.endStroke()

        assertEquals(1, viewModel.completedStrokes.value.size)
        assertTrue(viewModel.currentStroke.value.isEmpty())
    }

    @Test
    fun `endStroke with empty current stroke is a no-op`() {
        viewModel.endStroke()
        assertTrue(viewModel.completedStrokes.value.isEmpty())
    }

    @Test
    fun `undoLastStroke removes most recent completed stroke`() {
        repeat(3) {
            viewModel.startStroke(Offset(it.toFloat(), 0f))
            viewModel.endStroke()
        }
        assertEquals(3, viewModel.completedStrokes.value.size)

        viewModel.undoLastStroke()
        assertEquals(2, viewModel.completedStrokes.value.size)
    }

    @Test
    fun `undo on empty strokes is a no-op`() {
        viewModel.undoLastStroke()
        assertTrue(viewModel.completedStrokes.value.isEmpty())
    }

    @Test
    fun `clearDrawing removes all strokes and current stroke`() {
        viewModel.startStroke(Offset(0f, 0f))
        viewModel.continueStroke(Offset(5f, 5f))
        viewModel.endStroke()
        viewModel.startStroke(Offset(10f, 10f))

        viewModel.clearDrawing()

        assertTrue(viewModel.completedStrokes.value.isEmpty())
        assertTrue(viewModel.currentStroke.value.isEmpty())
    }

    @Test
    fun `hasDrawing is false when no strokes`() {
        assertFalse(viewModel.hasDrawing)
    }

    @Test
    fun `hasDrawing is true after a stroke is completed`() {
        viewModel.startStroke(Offset(0f, 0f))
        viewModel.endStroke()
        assertTrue(viewModel.hasDrawing)
    }

    // ── Image tab ─────────────────────────────────────────────────────────────

    @Test
    fun `onImageSelected sets selectedImageUri`() {
        val uri = mockk<android.net.Uri>()
        viewModel.onImageSelected(uri)
        assertEquals(uri, viewModel.uiState.value.selectedImageUri)
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun `deleteSignature removes from repository`() = runTest {
        val sig = Signature(
            id = "sig-1",
            name = "Test",
            type = SignatureType.DRAWN,
            createdAt = 0L,
        )
        fakeRepo.save(sig)

        viewModel.deleteSignature(sig)

        val remaining = fakeRepo.getAll().first()
        assertTrue(remaining.isEmpty())
    }
}
