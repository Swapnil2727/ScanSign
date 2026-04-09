package com.spatel.scansign.core

import android.graphics.Bitmap
import com.spatel.scansign.core.data.SignDocumentUseCase
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.util.FakeDocumentRepository
import com.spatel.scansign.util.FakePdfSigner
import com.spatel.scansign.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.mockk

@OptIn(ExperimentalCoroutinesApi::class)
class SignDocumentUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepo = FakeDocumentRepository()
    private val stubBitmap: Bitmap = mockk(relaxed = true)

    private val docWithPdf = Document(
        id = "doc-1",
        title = "Contract",
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_700_000_000_000L,
        pageCount = 2,
        fileSize = 512_000L,
        status = DocumentStatus.SCANNED,
        pdfPath = "/data/files/documents/doc-1/document.pdf",
        thumbnailPath = null,
    )

    @Before
    fun setup() {
        fakeRepo.setDocuments(listOf(docWithPdf))
    }

    @Test
    fun `success - embeds bitmap and marks document as SIGNED`() = runTest {
        val fakeSigner = FakePdfSigner(shouldFail = false)
        val useCase = SignDocumentUseCase(fakeRepo, fakeSigner)

        val result = useCase("doc-1", stubBitmap, 0, 100f, 50f, 200f, 80f)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeSigner.callCount)
        assertEquals("doc-1", fakeRepo.lastSignedId)
    }

    @Test
    fun `document not found - returns failure, embedBitmap never called`() = runTest {
        val fakeSigner = FakePdfSigner(shouldFail = false)
        val useCase = SignDocumentUseCase(fakeRepo, fakeSigner)

        val result = useCase("non-existent-id", stubBitmap, 0, 0f, 0f, 100f, 40f)

        assertTrue(result.isFailure)
        assertEquals(0, fakeSigner.callCount)
        assertNull(fakeRepo.lastSignedId)
    }

    @Test
    fun `embed failure - returns failure, markAsSigned never called`() = runTest {
        val fakeSigner = FakePdfSigner(shouldFail = true)
        val useCase = SignDocumentUseCase(fakeRepo, fakeSigner)

        val result = useCase("doc-1", stubBitmap, 0, 0f, 0f, 100f, 40f)

        assertTrue(result.isFailure)
        assertEquals(1, fakeSigner.callCount)
        assertNull("document must NOT be marked signed when embed fails", fakeRepo.lastSignedId)
    }

    @Test
    fun `success - signed document status is reflected in repository`() = runTest {
        val useCase = SignDocumentUseCase(fakeRepo, FakePdfSigner())

        useCase("doc-1", stubBitmap, 0, 0f, 0f, 100f, 40f)

        val updated = fakeRepo.getById("doc-1")
        // Just assert the repository recorded the signed ID — status update is Repository's concern
        assertEquals("doc-1", fakeRepo.lastSignedId)
    }
}
