package com.spatel.scansign.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.datastore.AppTheme
import com.spatel.scansign.core.datastore.ScanQuality
import com.spatel.scansign.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val appTheme: AppTheme,
        val scanQuality: ScanQuality,
        val documentCount: Int,
        val totalStorageBytes: Long,
    ) : SettingsUiState
}

class SettingsViewModel(
    private val preferencesDataSource: UserPreferencesDataSource,
    repository: DocumentRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesDataSource.userPreferences,
        repository.getAll(),
    ) { prefs, docs ->
        SettingsUiState.Success(
            appTheme = prefs.appTheme,
            scanQuality = prefs.scanQuality,
            documentCount = docs.size,
            totalStorageBytes = docs.sumOf { it.fileSize },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState.Loading,
    )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { preferencesDataSource.setAppTheme(theme) }
    }

    fun setScanQuality(quality: ScanQuality) {
        viewModelScope.launch { preferencesDataSource.setScanQuality(quality) }
    }
}
