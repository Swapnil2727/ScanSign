package com.spatel.scansign.di

import com.spatel.scansign.ui.documents.DocumentDetailViewModel
import com.spatel.scansign.ui.documents.DocumentsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val documentsModule = module {
    viewModel { DocumentsViewModel(get()) }
    viewModel { params -> DocumentDetailViewModel(params.get(), get()) }
}
