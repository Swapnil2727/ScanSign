package com.spatel.scansign.core.di

import androidx.room.Room
import com.spatel.scansign.core.database.ScanSignDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ScanSignDatabase::class.java,
            "scansign.db",
        ).build()
    }
    single { get<ScanSignDatabase>().documentDao() }
}
