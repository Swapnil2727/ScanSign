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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepo = FakeDocumentRepository()

    private val testDocument = Document(
        id = "doc-1",
        title = "Invoice March 2025",
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_700_000_000_000L,
        pageCount = 3,
        fileSize = 1_024_000L,
        status = DocumentStatus.SCANNED,
    )

    private fun viewModel(documentId: String = "doc-1") =
        DocumentDetailViewModel(documentId, fakeRepo)

    /** Activate the StateFlow (WhileSubscribed requires at least one collector). */
    private fun kotlinx.coroutines.test.TestScope.collectUiState(vm: DocumentDetailViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `initial uiState is Loading`() {
        val vm = viewModel()
        assertTrue(vm.uiState.value is DocumentDetailUiState.Loading)
    }

    @Test
    fun `uiState is Success when document exists`() = runTest {
        fakeRepo.setDocuments(listOf(testDocument))
        val vm = viewModel()
        collectUiState(vm)

        val state = vm.uiState.value
        assertTrue(state is DocumentDetailUiState.Success)
        assertEquals(testDocument, (state as DocumentDetailUiState.Success).document)
    }

    @Test
    fun `uiState is Error when document is not found`() = runTest {
        fakeRepo.setDocuments(emptyList())
        val vm = viewModel(documentId = "non-existent")
        collectUiState(vm)

        assertTrue(vm.uiState.value is DocumentDetailUiState.Error)
    }

    @Test
    fun `rename calls repository with new title`() = runTest {
        fakeRepo.setDocuments(listOf(testDocument))
        val vm = viewModel()
        collectUiState(vm)

        vm.rename("Updated Title")

        assertEquals("doc-1", fakeRepo.lastRenamedId)
        assertEquals("Updated Title", fakeRepo.lastRenamedTitle)
    }

    @Test
    fun `rename updates document title reactively in uiState`() = runTest {
        fakeRepo.setDocuments(listOf(testDocument))
        val vm = viewModel()
        collectUiState(vm)

        vm.rename("New Title")

        val state = vm.uiState.value as DocumentDetailUiState.Success
        assertEquals("New Title", state.document.title)
    }

    @Test
    fun `delete calls repository and emits Deleted event`() = runTest {
        fakeRepo.setDocuments(listOf(testDocument))
        val vm = viewModel()
        collectUiState(vm)

        val events = mutableListOf<DocumentDetailEvent>()
        val eventsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.events.collect { events.add(it) }
        }

        vm.delete()

        assertEquals("doc-1", fakeRepo.lastDeletedId)
        assertEquals(listOf(DocumentDetailEvent.Deleted), events)
    }
}
