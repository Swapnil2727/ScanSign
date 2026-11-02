package com.scansign.app.core.di

import com.scansign.app.core.data.DeleteDocumentUseCase
import com.scansign.app.core.data.DocumentRepository
import com.scansign.app.core.data.SaveScannedDocumentUseCase
import com.scansign.app.core.data.SignDocumentUseCase
import com.scansign.app.core.data.SignatureRepository
import com.scansign.app.core.pdf.PdfCopier
import com.scansign.app.core.pdf.PdfMetadata
import com.scansign.app.core.pdf.PdfPageRenderer
import com.scansign.app.core.pdf.PdfSigner
import com.scansign.app.core.signing.KeystoreManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { PdfCopier(androidContext()) }
    single { PdfMetadata() }
    single { PdfPageRenderer() }
    single { PdfSigner() }
    single { KeystoreManager() }
    single<DocumentRepository> { DocumentRepository(get(), get(), get(), androidContext()) }
    single<SignatureRepository> { SignatureRepository(get()) }
    single<SaveScannedDocumentUseCase> { SaveScannedDocumentUseCase(get<DocumentRepository>()) }
    single<DeleteDocumentUseCase> { DeleteDocumentUseCase(get<DocumentRepository>()) }
    single<SignDocumentUseCase> { SignDocumentUseCase(get<DocumentRepository>(), get<PdfSigner>(), get<PdfPageRenderer>()) }
}
