package com.spatel.scansign.core.di

import com.spatel.scansign.core.pdf.PdfCopier
import com.spatel.scansign.core.pdf.PdfMetadata
import com.spatel.scansign.core.pdf.PdfPageRenderer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { PdfCopier(androidContext()) }
    single { PdfMetadata() }
    single { PdfPageRenderer() }
}
