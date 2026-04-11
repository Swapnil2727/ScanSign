package com.spatel.scansign.di

import com.spatel.scansign.ui.onboarding.OnboardingViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    viewModel { OnboardingViewModel(get()) }
}
