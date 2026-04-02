package com.spatel.scansign.ui.settings

import com.spatel.scansign.core.datastore.AppTheme
import com.spatel.scansign.core.datastore.ScanQuality
import com.spatel.scansign.core.datastore.UserPreferencesDataSource
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentStatus
import com.spatel.scansign.util.FakeDocumentRepository
import com.spatel.scansign.util.FakePreferencesDataStore
import com.spatel.scansign.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeDataStore = FakePreferencesDataStore()
    private val dataSource = UserPreferencesDataSource(fakeDataStore)
    private val fakeRepo = FakeDocumentRepository()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(dataSource, fakeRepo)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun doc(id: String, fileSize: Long = 1024L) = Document(
        id = id,
        title = "Doc $id",
        createdAt = 0L,
        updatedAt = 0L,
        pageCount = 1,
        fileSize = fileSize,
        status = DocumentStatus.SCANNED,
    )

    /** WhileSubscribed(5_000) requires an active collector. */
    private fun kotlinx.coroutines.test.TestScope.collectUiState() =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        assertEquals(SettingsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `state becomes Success with defaults when no prefs or docs`() = runTest {
        collectUiState()

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(AppTheme.SYSTEM, state.appTheme)
        assertEquals(ScanQuality.STANDARD, state.scanQuality)
        assertEquals(0, state.documentCount)
        assertEquals(0L, state.totalStorageBytes)
    }

    // ── Document count + storage ──────────────────────────────────────────────

    @Test
    fun `documentCount reflects number of documents in repository`() = runTest {
        collectUiState()
        fakeRepo.setDocuments(listOf(doc("1"), doc("2"), doc("3")))

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(3, state.documentCount)
    }

    @Test
    fun `totalStorageBytes is sum of all document file sizes`() = runTest {
        collectUiState()
        fakeRepo.setDocuments(listOf(doc("1", 500L), doc("2", 1500L)))

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(2000L, state.totalStorageBytes)
    }

    @Test
    fun `totalStorageBytes is zero when repository is empty`() = runTest {
        collectUiState()

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(0L, state.totalStorageBytes)
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    @Test
    fun `setAppTheme updates appTheme in Success state`() = runTest {
        collectUiState()

        viewModel.setAppTheme(AppTheme.DARK)

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(AppTheme.DARK, state.appTheme)
    }

    @Test
    fun `setAppTheme to LIGHT reflects in state`() = runTest {
        collectUiState()

        viewModel.setAppTheme(AppTheme.LIGHT)

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(AppTheme.LIGHT, state.appTheme)
    }

    @Test
    fun `setAppTheme back to SYSTEM reflects in state`() = runTest {
        collectUiState()

        viewModel.setAppTheme(AppTheme.DARK)
        viewModel.setAppTheme(AppTheme.SYSTEM)

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(AppTheme.SYSTEM, state.appTheme)
    }

    // ── Scan quality ──────────────────────────────────────────────────────────

    @Test
    fun `setScanQuality updates scanQuality in Success state`() = runTest {
        collectUiState()

        viewModel.setScanQuality(ScanQuality.HIGH)

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(ScanQuality.HIGH, state.scanQuality)
    }

    @Test
    fun `setScanQuality back to STANDARD reflects in state`() = runTest {
        collectUiState()

        viewModel.setScanQuality(ScanQuality.HIGH)
        viewModel.setScanQuality(ScanQuality.STANDARD)

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(ScanQuality.STANDARD, state.scanQuality)
    }

    // ── Combined ──────────────────────────────────────────────────────────────

    @Test
    fun `theme and quality changes are independent`() = runTest {
        collectUiState()

        viewModel.setAppTheme(AppTheme.DARK)
        viewModel.setScanQuality(ScanQuality.HIGH)

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(AppTheme.DARK, state.appTheme)
        assertEquals(ScanQuality.HIGH, state.scanQuality)
    }

    @Test
    fun `document list update does not reset theme preference`() = runTest {
        collectUiState()

        viewModel.setAppTheme(AppTheme.DARK)
        fakeRepo.setDocuments(listOf(doc("1")))

        val state = viewModel.uiState.value as SettingsUiState.Success
        assertEquals(AppTheme.DARK, state.appTheme)
        assertEquals(1, state.documentCount)
    }
}
