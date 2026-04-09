package com.spatel.scansign.core.di

import com.spatel.scansign.core.data.DeleteDocumentUseCase
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.data.SaveScannedDocumentUseCase
import com.spatel.scansign.core.data.SignDocumentUseCase
import com.spatel.scansign.core.data.SignatureRepository
import com.spatel.scansign.core.pdf.PdfCopier
import com.spatel.scansign.core.pdf.PdfMetadata
import com.spatel.scansign.core.pdf.PdfPageRenderer
import com.spatel.scansign.core.pdf.PdfSigner
import com.spatel.scansign.core.signing.KeystoreManager
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
    single<SignDocumentUseCase> { SignDocumentUseCase(get<DocumentRepository>(), get<PdfSigner>()) }
}
