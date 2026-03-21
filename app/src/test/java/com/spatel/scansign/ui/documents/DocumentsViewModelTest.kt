package com.spatel.scansign.ui.documents

import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.util.FakeDocumentRepository
import com.spatel.scansign.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepo = FakeDocumentRepository()
    private val deletedIds = mutableListOf<String>()
    private val fakeDelete = com.spatel.scansign.core.data.DeleteDocumentUseCase { id ->
        deletedIds.add(id)
    }

    private lateinit var viewModel: DocumentsViewModel

    @Before
    fun setup() {
        viewModel = DocumentsViewModel(fakeRepo, fakeDelete)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun doc(id: String, title: String) = Document(
        id = id,
        title = title,
        createdAt = 0L,
        updatedAt = 0L,
        pageCount = 1,
        fileSize = 1024L,
        status = DocumentStatus.SCANNED,
    )

    /** Activate the StateFlow (WhileSubscribed requires at least one collector). */
    private fun kotlinx.coroutines.test.TestScope.collectDocuments() =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.documents.collect {}
        }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `initial documents list is empty`() = runTest {
        collectDocuments()
        assertEquals(emptyList<Document>(), viewModel.documents.value)
    }

    @Test
    fun `documents emits all items from repository`() = runTest {
        collectDocuments()
        val docs = listOf(doc("1", "Invoice"), doc("2", "Contract"))
        fakeRepo.setDocuments(docs)
        assertEquals(docs, viewModel.documents.value)
    }

    @Test
    fun `search filters documents by title case-insensitively`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice March"), doc("2", "Contract 2025")))

        viewModel.onSearchQueryChange("invoice")

        assertEquals(listOf(doc("1", "Invoice March")), viewModel.documents.value)
    }

    @Test
    fun `blank search query shows all documents`() = runTest {
        collectDocuments()
        val docs = listOf(doc("1", "Invoice"), doc("2", "Contract"))
        fakeRepo.setDocuments(docs)

        viewModel.onSearchQueryChange("invoice")
        viewModel.onSearchQueryChange("")

        assertEquals(docs, viewModel.documents.value)
    }

    @Test
    fun `search with no matching title returns empty list`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice"), doc("2", "Contract")))

        viewModel.onSearchQueryChange("receipt")

        assertEquals(emptyList<Document>(), viewModel.documents.value)
    }

    @Test
    fun `requestDelete hides document immediately without calling delete`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice"), doc("2", "Contract")))

        viewModel.requestDelete("1")

        assertEquals(listOf(doc("2", "Contract")), viewModel.documents.value)
        assertEquals(emptyList<String>(), deletedIds) // not deleted yet
    }

    @Test
    fun `undoDelete restores the hidden document`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice"), doc("2", "Contract")))

        viewModel.requestDelete("1")
        viewModel.undoDelete()

        assertEquals(listOf(doc("1", "Invoice"), doc("2", "Contract")), viewModel.documents.value)
        assertEquals(emptyList<String>(), deletedIds)
    }

    @Test
    fun `confirmDelete calls delete use case with the correct id`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice")))

        viewModel.requestDelete("1")
        viewModel.confirmDelete()

        assertEquals(listOf("1"), deletedIds)
    }

    @Test
    fun `confirmDelete after undoDelete does not call delete`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice")))

        viewModel.requestDelete("1")
        viewModel.undoDelete()
        viewModel.confirmDelete()

        assertEquals(emptyList<String>(), deletedIds)
    }

    @Test
    fun `pending delete item excluded from search results`() = runTest {
        collectDocuments()
        fakeRepo.setDocuments(listOf(doc("1", "Invoice"), doc("2", "Contract")))

        viewModel.requestDelete("1")
        viewModel.onSearchQueryChange("invoice")

        assertEquals(emptyList<Document>(), viewModel.documents.value)
    }
}
