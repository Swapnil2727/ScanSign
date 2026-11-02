package com.scansign.app.di

import com.scansign.app.ui.documents.DocumentDetailViewModel
import com.scansign.app.ui.documents.DocumentSigningViewModel
import com.scansign.app.ui.documents.DocumentViewerViewModel
import com.scansign.app.ui.documents.DocumentsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val documentsModule = module {
    viewModel { DocumentsViewModel(get(), get()) }
    viewModel { params -> DocumentDetailViewModel(params.get(), get()) }
    viewModel { params -> DocumentViewerViewModel(params.get(), get()) }
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
