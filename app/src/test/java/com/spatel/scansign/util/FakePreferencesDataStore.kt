package com.spatel.scansign.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory implementation of [DataStore]<[Preferences]> for unit tests.
 * Passed directly to [com.spatel.scansign.core.datastore.UserPreferencesDataSource]
 * so the real mapping/parsing logic is exercised without file I/O or mocks.
 */
class FakePreferencesDataStore : DataStore<Preferences> {

    private val _data = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = _data

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(_data.value)
        _data.value = updated
        return updated
    }
}
