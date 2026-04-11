package com.spatel.scansign.ui.documents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import com.spatel.scansign.core.data.SignDocumentUseCase
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.model.SignatureType
import com.spatel.scansign.core.pdf.PdfPageRenderer
import com.spatel.scansign.util.FakeDocumentRepository
import com.spatel.scansign.util.FakeSignatureRepository
import com.spatel.scansign.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentSigningViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeDocumentRepo  = FakeDocumentRepository()
    private val fakeSignatureRepo = FakeSignatureRepository()
    private val mockContext: Context = mockk(relaxed = true)

    // Fake PdfPageRenderer — returns a predictable 1080×1400 bitmap and 612×792 pt page size.
    // PdfPageRenderer is now `open` so we can subclass without mockk.
    private val fakePdfPageRenderer = object : PdfPageRenderer() {
        override suspend fun renderPage(
            pdfFile: File, pageIndex: Int, widthPx: Int,
        ): Result<Bitmap> {
            val bmp = mockk<Bitmap>(relaxed = true)
            every { bmp.width }  returns 1080
            every { bmp.height } returns 1400
            return Result.success(bmp)
        }

        override suspend fun getPageSizePt(
            pdfFile: File, pageIndex: Int,
        ): Result<Pair<Int, Int>> = Result.success(Pair(612, 792))
    }

    // Use the fun interface lambda — no mocks needed.
    private var fakeSignResult: Result<Unit> = Result.success(Unit)
    private val fakeSignUseCase = SignDocumentUseCase { _, _, _, _, _, _, _ -> fakeSignResult }

    private val testDoc = Document(
        id            = "doc-1",
        title         = "Contract",
        createdAt     = 0L,
        updatedAt     = 0L,
        pageCount     = 3,
        fileSize      = 100_000L,
        status        = DocumentStatus.SCANNED,
        pdfPath       = "/data/user/0/test/documents/doc-1/document.pdf",
    )

    @Before
    fun setup() {
        fakeDocumentRepo.setDocuments(listOf(testDoc))
        fakeSignResult = Result.success(Unit)
    }

    private fun createViewModel() = DocumentSigningViewModel(
        documentId          = "doc-1",
        documentRepository  = fakeDocumentRepo,
        signatureRepository = fakeSignatureRepo,
        pdfPageRenderer     = fakePdfPageRenderer,
        signDocumentUseCase = fakeSignUseCase,
        context             = mockContext,
    )

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has no selected signature and signingState is Idle`() = runTest {
        val vm = createViewModel()

        assertNull(vm.uiState.value.selectedSignature)
        assertTrue(vm.uiState.value.signingState is SigningState.Idle)
    }

    // ── selectSignature ───────────────────────────────────────────────────────

    @Test
    fun `selectSignature sets selected signature and centres offset on page`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()  // allow init coroutine to complete and bitmap to load

        val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN, bitmapPath = null)
        vm.selectSignature(sig)

        assertEquals(sig, vm.uiState.value.selectedSignature)
        // bmp 1080×1400; sigW = 1080*0.25 = 270; sigH = 270/3 = 90
        // centreX = (1080 - 270) / 2 = 405; centreY = (1400 - 90) / 2 = 655
        val offset = vm.signatureOffset.value
        assertEquals(405f, offset.x, 0.01f)
        assertEquals(655f, offset.y, 0.01f)
    }

    // ── dragSignature ─────────────────────────────────────────────────────────

    @Test
    fun `dragSignature updates offset by delta`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN)
        vm.selectSignature(sig)
        val initial = vm.signatureOffset.value

        vm.dragSignature(Offset(50f, -30f))

        val updated = vm.signatureOffset.value
        assertEquals(initial.x + 50f, updated.x, 0.01f)
        // -30 would go to 625, which is above 0, so no clamping
        assertEquals((initial.y - 30f).coerceAtLeast(0f), updated.y, 0.01f)
    }

    @Test
    fun `dragSignature clamps offset so signature stays within bitmap bounds`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN)
        vm.selectSignature(sig)

        vm.dragSignature(Offset(10_000f, 10_000f))

        val offset = vm.signatureOffset.value
        val size   = vm.signatureSize.value
        // bitmap 1080 wide, sigW 270 → max X = 810
        assertEquals((1080f - size.width).coerceAtLeast(0f), offset.x, 0.01f)
        // bitmap 1400 tall, sigH 90 → max Y = 1310
        assertEquals((1400f - size.height).coerceAtLeast(0f), offset.y, 0.01f)
    }

    // ── Page navigation ───────────────────────────────────────────────────────

    @Test
    fun `nextPage increments pageIndex and is clamped at pageCount minus 1`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()  // init sets pageCount = 3

        assertEquals(0, vm.uiState.value.pageIndex)

        vm.nextPage(); advanceUntilIdle()
        assertEquals(1, vm.uiState.value.pageIndex)

        vm.nextPage(); advanceUntilIdle()
        assertEquals(2, vm.uiState.value.pageIndex)

        vm.nextPage(); advanceUntilIdle()  // already at last page
        assertEquals(2, vm.uiState.value.pageIndex)
    }

    @Test
    fun `prevPage decrements pageIndex and is clamped at 0`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.nextPage(); advanceUntilIdle()
        assertEquals(1, vm.uiState.value.pageIndex)

        vm.prevPage(); advanceUntilIdle()
        assertEquals(0, vm.uiState.value.pageIndex)

        vm.prevPage(); advanceUntilIdle()  // already at first page
        assertEquals(0, vm.uiState.value.pageIndex)
    }

    // ── confirm ───────────────────────────────────────────────────────────────

    @Test
    fun `confirm with no selected signature is a no-op`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.confirm()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.signingState is SigningState.Idle)
        assertNull(fakeDocumentRepo.lastSignedId)
    }

    @Test
    fun `confirm calls use case and emits Success`() = runTest {
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockk(relaxed = true)

        try {
            val vm = createViewModel()
                advanceUntilIdle()

            val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN, bitmapPath = "/fake/path.png")
            vm.selectSignature(sig)
            vm.confirm()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.signingState is SigningState.Success)
        } finally {
            unmockkStatic(BitmapFactory::class)
        }
    }

    @Test
    fun `confirm emits Error when use case fails`() = runTest {
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockk(relaxed = true)
        fakeSignResult = Result.failure(RuntimeException("PDF write error"))

        try {
            val vm = createViewModel()
                advanceUntilIdle()

            val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN, bitmapPath = "/fake/path.png")
            vm.selectSignature(sig)
            vm.confirm()
            advanceUntilIdle()

            val state = vm.uiState.value.signingState
            assertTrue(state is SigningState.Error)
            assertEquals("PDF write error", (state as SigningState.Error).message)
        } finally {
            unmockkStatic(BitmapFactory::class)
        }
    }

    // ── resizeSignature ───────────────────────────────────────────────────────

    @Test
    fun `resizeSignature increases size by delta within page bounds`() = runTest {
        val vm = createViewModel()
        activateFlows(vm)
        advanceUntilIdle()

        val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN)
        vm.selectSignature(sig)
        // Initial size: width=270, height=90 (25% of 1080, 3:1 aspect)
        val initialSize = vm.signatureSize.value

        vm.resizeSignature(Offset(50f, 30f))

        val updated = vm.signatureSize.value
        assertEquals(initialSize.width  + 50f, updated.width,  0.01f)
        assertEquals(initialSize.height + 30f, updated.height, 0.01f)
    }

    @Test
    fun `resizeSignature clamps at minimum size of 5 percent page width`() = runTest {
        val vm = createViewModel()
        activateFlows(vm)
        advanceUntilIdle()

        val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN)
        vm.selectSignature(sig)

        // Shrink far below minimum (5% of 1080 = 54px)
        vm.resizeSignature(Offset(-10_000f, -10_000f))

        val size = vm.signatureSize.value
        val minPx = 1080f * 0.05f   // 54px
        assertEquals(minPx, size.width,  0.01f)
        assertEquals(minPx, size.height, 0.01f)
    }

    // ── clearSigningState ─────────────────────────────────────────────────────

    @Test
    fun `clearSigningState resets to Idle`() = runTest {
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockk(relaxed = true)
        fakeSignResult = Result.failure(RuntimeException("error"))

        try {
            val vm = createViewModel()
                advanceUntilIdle()

            val sig = Signature("s1", "My Sig", 0L, SignatureType.DRAWN, bitmapPath = "/fake/path.png")
            vm.selectSignature(sig)
            vm.confirm()
            advanceUntilIdle()

            assertFalse(vm.uiState.value.signingState is SigningState.Idle)

            vm.clearSigningState()
            assertTrue(vm.uiState.value.signingState is SigningState.Idle)
        } finally {
            unmockkStatic(BitmapFactory::class)
        }
    }
}
