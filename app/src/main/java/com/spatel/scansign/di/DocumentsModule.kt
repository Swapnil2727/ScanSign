package com.spatel.scansign.di

import com.spatel.scansign.ui.documents.DocumentDetailViewModel
import com.spatel.scansign.ui.documents.DocumentSigningViewModel
import com.spatel.scansign.ui.documents.DocumentsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val documentsModule = module {
    viewModel { DocumentsViewModel(get(), get()) }
    viewModel { params -> DocumentDetailViewModel(params.get(), get()) }
    viewModel { params ->
        DocumentSigningViewModel(
            documentId          = params.get(),
            documentRepository  = get(),
            signatureRepository = get(),
            pdfPageRenderer     = get(),
            signDocumentUseCase = get(),
            context             = androidContext(),
        )
    }
}
