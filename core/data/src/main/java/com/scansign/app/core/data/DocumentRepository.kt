package com.scansign.app.core.data

import android.content.Context
import android.net.Uri
import com.scansign.app.core.database.DocumentDao
import com.scansign.app.core.model.Document
import com.scansign.app.core.model.DocumentPage
import com.scansign.app.core.pdf.PdfCopier
import com.scansign.app.core.pdf.PdfMetadata
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    suspend fun save(pdfUri: Uri, pageUris: List<Uri>, title: String): Result<Document>

    fun getAll(): Flow<List<Document>>

    fun getById(id: String): Flow<Document?>

    suspend fun getPages(documentId: String): List<DocumentPage>

    suspend fun rename(id: String, title: String)

    suspend fun delete(id: String)

    suspend fun markAsSigned(id: String)

    companion object {
        operator fun invoke(
            dao: DocumentDao,
            pdfCopier: PdfCopier,
            pdfMetadata: PdfMetadata,
            context: Context,
        ): DocumentRepository = DocumentRepositoryImpl(dao, pdfCopier, pdfMetadata, context)
    }
}
