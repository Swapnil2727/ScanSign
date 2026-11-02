package com.scansign.app.di

import com.scansign.app.core.pdf.ImagesToPdfConverter
import com.scansign.app.ui.scanner.ScannerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val scannerModule = module {
    single { ImagesToPdfConverter(androidContext()) }
    viewModel { ScannerViewModel(get(), get()) }
}
