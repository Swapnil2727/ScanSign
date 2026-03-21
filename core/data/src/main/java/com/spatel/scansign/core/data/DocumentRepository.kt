package com.spatel.scansign.core.data

import android.content.Context
import android.net.Uri
import com.spatel.scansign.core.database.DocumentDao
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentPage
import com.spatel.scansign.core.pdf.PdfCopier
import com.spatel.scansign.core.pdf.PdfMetadata
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    suspend fun save(pdfUri: Uri, pageUris: List<Uri>, title: String): Result<Document>

    fun getAll(): Flow<List<Document>>

    fun getById(id: String): Flow<Document?>

    suspend fun getPages(documentId: String): List<DocumentPage>

    suspend fun rename(id: String, title: String)

    suspend fun delete(id: String)

    companion object {
        operator fun invoke(
            dao: DocumentDao,
            pdfCopier: PdfCopier,
            pdfMetadata: PdfMetadata,
            context: Context,
        ): DocumentRepository = DocumentRepositoryImpl(dao, pdfCopier, pdfMetadata, context)
    }
}
