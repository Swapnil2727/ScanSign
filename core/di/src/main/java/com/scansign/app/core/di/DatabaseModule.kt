package com.scansign.app.core.di

import androidx.room.Room
import com.scansign.app.core.database.MIGRATION_1_2
import com.scansign.app.core.database.ScanSignDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ScanSignDatabase::class.java,
            "scansign.db",
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { get<ScanSignDatabase>().documentDao() }
    single { get<ScanSignDatabase>().signatureDao() }
}
