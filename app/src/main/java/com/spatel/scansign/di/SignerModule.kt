package com.spatel.scansign.di

import com.spatel.scansign.ui.signer.SignerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val signerModule = module {
    viewModel { SignerViewModel(get(), androidContext()) }
}
