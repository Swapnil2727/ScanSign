package com.scansign.app.core.data

import android.content.Context
import android.net.Uri
import com.scansign.app.core.database.DocumentDao
import com.scansign.app.core.database.DocumentEntity
import com.scansign.app.core.database.DocumentPageEntity
import com.scansign.app.core.model.Document
import com.scansign.app.core.model.DocumentPage
import com.scansign.app.core.model.DocumentStatus
import com.scansign.app.core.pdf.PdfCopier
import com.scansign.app.core.pdf.PdfMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

internal class DocumentRepositoryImpl(
    private val dao: DocumentDao,
    private val pdfCopier: PdfCopier,
    private val pdfMetadata: PdfMetadata,
    private val context: Context,
) : DocumentRepository {

    override suspend fun save(
        pdfUri: Uri,
        pageUris: List<Uri>,
        title: String,
    ): Result<Document> = withContext(Dispatchers.IO) {
        runCatching {
            val documentId = UUID.randomUUID().toString()
            val docDir = File(context.filesDir, "documents/$documentId").also { it.mkdirs() }

            val pdfFile = pdfCopier.copy(pdfUri, File(docDir, "document.pdf")).getOrThrow()
            val info = pdfMetadata.read(pdfFile).getOrThrow()

            val pageFiles = pageUris.mapIndexed { index, uri ->
                pdfCopier.copy(uri, File(docDir, "page_$index.jpg")).getOrThrow()
            }

            val now = System.currentTimeMillis()
            val entity = DocumentEntity(
                id = documentId,
                title = title,
                pdfPath = pdfFile.absolutePath,
                thumbnailPath = pageFiles.firstOrNull()?.absolutePath,
                pageCount = info.pageCount,
                fileSizeBytes = info.fileSizeBytes,
                status = DocumentStatus.SCANNED.name,
                createdAt = now,
                updatedAt = now,
            )
            val pageEntities = pageFiles.mapIndexed { index, file ->
                DocumentPageEntity(
                    id = UUID.randomUUID().toString(),
                    documentId = documentId,
                    pageNumber = index,
                    imagePath = file.absolutePath,
                )
            }

            dao.insert(entity)
            dao.insertPages(pageEntities)
            entity.toDomain()
        }
    }

    override fun getAll(): Flow<List<Document>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Document?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getPages(documentId: String): List<DocumentPage> =
        withContext(Dispatchers.IO) {
            dao.getPagesByDocumentId(documentId).map { it.toDomain() }
        }

    override suspend fun rename(id: String, title: String) {
        withContext(Dispatchers.IO) {
            dao.updateTitle(id, title, System.currentTimeMillis())
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            File(context.filesDir, "documents/$id").deleteRecursively()
        }
        dao.delete(id)
    }

    override suspend fun markAsSigned(id: String) {
        withContext(Dispatchers.IO) {
            dao.updateStatus(id, DocumentStatus.SIGNED.name, System.currentTimeMillis())
        }
    }
}
