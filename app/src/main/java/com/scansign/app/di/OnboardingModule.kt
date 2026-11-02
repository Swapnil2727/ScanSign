package com.scansign.app.di

import com.scansign.app.ui.onboarding.OnboardingViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    viewModel { OnboardingViewModel(get()) }
}
