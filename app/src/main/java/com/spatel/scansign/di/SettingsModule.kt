package com.spatel.scansign.di

import com.spatel.scansign.MainViewModel
import com.spatel.scansign.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
