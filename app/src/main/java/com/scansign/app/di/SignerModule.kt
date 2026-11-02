package com.scansign.app.di

import com.scansign.app.ui.signer.SignerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val signerModule = module {
    viewModel { SignerViewModel(get(), androidContext()) }
}
