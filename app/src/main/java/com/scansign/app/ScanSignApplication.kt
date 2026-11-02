package com.scansign.app

import android.app.Application
import com.scansign.app.core.di.appModule
import com.scansign.app.core.di.databaseModule
import com.scansign.app.core.di.datastoreModule
import com.scansign.app.di.documentsModule
import com.scansign.app.di.onboardingModule
import com.scansign.app.di.scannerModule
import com.scansign.app.di.settingsModule
import com.scansign.app.di.signerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class ScanSignApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger()
                androidContext(this@ScanSignApplication)
                modules(appModule, databaseModule, datastoreModule, scannerModule, documentsModule, settingsModule, signerModule, onboardingModule)
            }
        }
    }
}
