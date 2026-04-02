package com.spatel.scansign.di

import com.spatel.scansign.core.pdf.ImagesToPdfConverter
import com.spatel.scansign.ui.scanner.ScannerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val scannerModule = module {
    single { ImagesToPdfConverter(androidContext()) }
    viewModel { ScannerViewModel(get(), get()) }
}
