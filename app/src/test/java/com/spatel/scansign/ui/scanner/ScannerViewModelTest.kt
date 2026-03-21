package com.spatel.scansign.ui.scanner

import android.net.Uri
import com.spatel.scansign.core.data.SaveScannedDocumentUseCase
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.util.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeDocument = Document(
        id = "doc-1",
        title = "Scan",
        createdAt = 0L,
        updatedAt = 0L,
        pageCount = 1,
        fileSize = 1024L,
        status = DocumentStatus.SCANNED,
    )

    // mockk is permitted here solely to stub android.net.Uri — an Android SDK abstract class
    // with no domain behaviour that cannot be faked with a plain implementation.
    private val pdfUri: Uri = mockk()
    private val pageUri: Uri = mockk()

    private fun viewModelWith(
        saveResult: Result<Document> = Result.success(fakeDocument),
    ): ScannerViewModel {
        val useCase = SaveScannedDocumentUseCase { _, _, _ -> saveResult }
        return ScannerViewModel(useCase)
    }

    // ── Scan result state ─────────────────────────────────────────────────────

    @Test
    fun `initial scanResult is null`() {
        assertNull(viewModelWith().scanResult.value)
    }

    @Test
    fun `onScanSuccess sets pdfUri and pageUris`() {
        val vm = viewModelWith()
        vm.onScanSuccess(pdfUri, listOf(pageUri))

        val result = vm.scanResult.value
        assertEquals(pdfUri, result?.pdfUri)
        assertEquals(listOf(pageUri), result?.pageUris)
    }

    @Test
    fun `clearScanResult resets scanResult to null`() {
        val vm = viewModelWith()
        vm.onScanSuccess(pdfUri, emptyList())
        vm.clearScanResult()

        assertNull(vm.scanResult.value)
    }

    @Test
    fun `clearScanResult resets saveState to Idle`() {
        val vm = viewModelWith()
        vm.onScanSuccess(pdfUri, emptyList())
        vm.clearScanResult()

        assertTrue(vm.saveState.value is SaveState.Idle)
    }

    @Test
    fun `onScanSuccess with empty pageUris is valid`() {
        val vm = viewModelWith()
        vm.onScanSuccess(pdfUri, emptyList())

        assertEquals(emptyList<Uri>(), vm.scanResult.value?.pageUris)
    }

    @Test
    fun `onScanSuccess replaces previous scan result`() {
        val vm = viewModelWith()
        val secondPdf: Uri = mockk()
        vm.onScanSuccess(pdfUri, emptyList())
        vm.onScanSuccess(secondPdf, emptyList())

        assertEquals(secondPdf, vm.scanResult.value?.pdfUri)
    }

    // ── Save state ────────────────────────────────────────────────────────────

    @Test
    fun `initial saveState is Idle`() {
        assertTrue(viewModelWith().saveState.value is SaveState.Idle)
    }

    @Test
    fun `save transitions to Success on successful save`() = runTest {
        val vm = viewModelWith(saveResult = Result.success(fakeDocument))
        vm.onScanSuccess(pdfUri, listOf(pageUri))

        vm.save("My Scan")

        val state = vm.saveState.value
        assertTrue(state is SaveState.Success)
        assertEquals(fakeDocument, (state as SaveState.Success).document)
    }

    @Test
    fun `save transitions to Error on failed save`() = runTest {
        val vm = viewModelWith(saveResult = Result.failure(RuntimeException("Disk full")))
        vm.onScanSuccess(pdfUri, listOf(pageUri))

        vm.save("My Scan")

        val state = vm.saveState.value
        assertTrue(state is SaveState.Error)
        assertEquals("Disk full", (state as SaveState.Error).message)
    }

    @Test
    fun `save does nothing when there is no scan result`() = runTest {
        val vm = viewModelWith()

        vm.save("My Scan")

        assertTrue(vm.saveState.value is SaveState.Idle)
    }
}
