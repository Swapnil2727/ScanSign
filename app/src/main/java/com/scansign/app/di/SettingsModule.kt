package com.scansign.app.di

import com.scansign.app.MainViewModel
import com.scansign.app.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
