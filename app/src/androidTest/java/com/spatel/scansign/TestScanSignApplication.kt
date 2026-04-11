package com.spatel.scansign

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.data.SignatureRepository
import com.spatel.scansign.core.di.appModule
import com.spatel.scansign.core.di.databaseModule
import com.spatel.scansign.core.di.datastoreModule
import com.spatel.scansign.di.documentsModule
import com.spatel.scansign.di.scannerModule
import com.spatel.scansign.di.settingsModule
import com.spatel.scansign.di.signerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TestScanSignApplication : Application() {

    val fakeDocumentRepository = FakeDocumentRepository()
    val fakeSignatureRepository = FakeSignatureRepository()
    val fakePreferencesDataStore = FakePreferencesDataStore()

    override fun onCreate() {
        super.onCreate()

        val testOverridesModule = module {
            single<DocumentRepository> { fakeDocumentRepository }
            single<SignatureRepository> { fakeSignatureRepository }
            single<DataStore<Preferences>> { fakePreferencesDataStore }
        }

        startKoin {
            androidLogger()
            androidContext(this@TestScanSignApplication)
            modules(
                testOverridesModule,  // Load test overrides FIRST
                appModule,
                databaseModule,
                datastoreModule,
                scannerModule,
                documentsModule,
                settingsModule,
                signerModule,
            )
        }
    }
}
