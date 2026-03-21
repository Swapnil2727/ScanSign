package com.spatel.scansign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.datastore.UserPreferences
import com.spatel.scansign.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Holds app-level state that must be ready before the first frame — specifically the
 * theme preference. Uses [SharingStarted.Eagerly] so the correct theme is available
 * immediately on cold start, preventing a flash of the wrong theme.
 */
class MainViewModel(
    dataSource: UserPreferencesDataSource,
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = dataSource.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences(),
        )
}
