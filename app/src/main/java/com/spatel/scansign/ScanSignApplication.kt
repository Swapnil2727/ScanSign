package com.spatel.scansign

import android.app.Application
import com.spatel.scansign.core.di.appModule
import com.spatel.scansign.core.di.databaseModule
import com.spatel.scansign.di.documentsModule
import com.spatel.scansign.di.scannerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ScanSignApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ScanSignApplication)
            modules(appModule, databaseModule, scannerModule, documentsModule)
        }
    }
}
