package com.spatel.scansign

import com.spatel.scansign.core.datastore.AppTheme
import com.spatel.scansign.core.datastore.ScanQuality
import com.spatel.scansign.core.datastore.UserPreferences
import com.spatel.scansign.core.datastore.UserPreferencesDataSource
import com.spatel.scansign.util.FakePreferencesDataStore
import com.spatel.scansign.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeDataStore = FakePreferencesDataStore()
    private val dataSource = UserPreferencesDataSource(fakeDataStore)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        viewModel = MainViewModel(dataSource)
    }

    @Test
    fun `initial userPreferences has system theme and standard quality`() = runTest {
        assertEquals(
            UserPreferences(appTheme = AppTheme.SYSTEM, scanQuality = ScanQuality.STANDARD),
            viewModel.userPreferences.value,
        )
    }

    @Test
    fun `userPreferences reflects theme change written to datastore`() = runTest {
        dataSource.setAppTheme(AppTheme.DARK)

        assertEquals(AppTheme.DARK, viewModel.userPreferences.value.appTheme)
    }

    @Test
    fun `userPreferences reflects light theme`() = runTest {
        dataSource.setAppTheme(AppTheme.LIGHT)

        assertEquals(AppTheme.LIGHT, viewModel.userPreferences.value.appTheme)
    }

    @Test
    fun `userPreferences reflects scan quality change`() = runTest {
        dataSource.setScanQuality(ScanQuality.HIGH)

        assertEquals(ScanQuality.HIGH, viewModel.userPreferences.value.scanQuality)
    }

    @Test
    fun `theme and quality can be changed independently`() = runTest {
        dataSource.setAppTheme(AppTheme.DARK)
        dataSource.setScanQuality(ScanQuality.HIGH)

        val prefs = viewModel.userPreferences.value
        assertEquals(AppTheme.DARK, prefs.appTheme)
        assertEquals(ScanQuality.HIGH, prefs.scanQuality)
    }
}
