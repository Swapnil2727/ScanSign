package com.scansign.app.core.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.scansign.app.core.datastore.UserPreferencesDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

val datastoreModule = module {
    single {
        PreferenceDataStoreFactory.create(
            produceFile = {
                File(androidContext().filesDir, "datastore/user_preferences.preferences_pb")
            },
        )
    }
    single { UserPreferencesDataSource(get()) }
}
