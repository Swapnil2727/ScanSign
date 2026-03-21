package com.spatel.scansign.ui.scanner

import android.net.Uri
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScannerViewModelTest {

    private val viewModel = ScannerViewModel()

    @Test
    fun `initial scanResult is null`() {
        assertNull(viewModel.scanResult.value)
    }

    @Test
    fun `onScanSuccess sets pdfUri and pageUris`() {
        val pdfUri = mockk<Uri>()
        val page1 = mockk<Uri>()
        val page2 = mockk<Uri>()

        viewModel.onScanSuccess(pdfUri, listOf(page1, page2))

        val result = viewModel.scanResult.value
        assertEquals(pdfUri, result?.pdfUri)
        assertEquals(listOf(page1, page2), result?.pageUris)
    }

    @Test
    fun `clearScanResult resets state to null`() {
        val pdfUri = mockk<Uri>()
        viewModel.onScanSuccess(pdfUri, emptyList())

        viewModel.clearScanResult()

        assertNull(viewModel.scanResult.value)
    }

    @Test
    fun `onScanSuccess with empty pageUris is valid`() {
        val pdfUri = mockk<Uri>()

        viewModel.onScanSuccess(pdfUri, emptyList())

        val result = viewModel.scanResult.value
        assertEquals(pdfUri, result?.pdfUri)
        assertEquals(emptyList<Uri>(), result?.pageUris)
    }

    @Test
    fun `onScanSuccess replaces previous result`() {
        val firstPdf = mockk<Uri>()
        val secondPdf = mockk<Uri>()

        viewModel.onScanSuccess(firstPdf, emptyList())
        viewModel.onScanSuccess(secondPdf, emptyList())

        assertEquals(secondPdf, viewModel.scanResult.value?.pdfUri)
    }
}
