package com.scansign.app.core.data

import android.net.Uri
import com.scansign.app.core.model.Document

fun interface SaveScannedDocumentUseCase {

    suspend operator fun invoke(pdfUri: Uri, pageUris: List<Uri>, title: String): Result<Document>

    companion object {
        operator fun invoke(repository: DocumentRepository): SaveScannedDocumentUseCase =
            SaveScannedDocumentUseCase { pdfUri, pageUris, title ->
                repository.save(pdfUri, pageUris, title)
            }
    }
}
