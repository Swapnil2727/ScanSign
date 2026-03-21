package com.spatel.scansign.di

import com.spatel.scansign.ui.scanner.ScannerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val scannerModule = module {
    viewModel { ScannerViewModel(get()) }
}
