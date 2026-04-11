package com.spatel.scansign

import android.net.Uri
import com.spatel.scansign.core.data.DocumentRepository
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory fake for [DocumentRepository]. Used in instrumented tests.
 */
class FakeDocumentRepository : DocumentRepository {

    private val _docs = MutableStateFlow<List<Document>>(emptyList())

    fun setDocuments(docs: List<Document>) {
        _docs.value = docs
    }

    override fun getAll(): Flow<List<Document>> = _docs

    override fun getById(id: String): Flow<Document?> =
        _docs.map { docs -> docs.find { it.id == id } }

    override suspend fun getPages(documentId: String): List<DocumentPage> = emptyList()

    override suspend fun rename(id: String, title: String) {
        _docs.update { docs ->
            docs.map { if (it.id == id) it.copy(title = title) else it }
        }
    }

    override suspend fun delete(id: String) {
        _docs.update { it.filter { doc -> doc.id != id } }
    }

    override suspend fun markAsSigned(id: String) {
        _docs.update { docs ->
            docs.map { if (it.id == id) it.copy(status = com.spatel.scansign.core.model.DocumentStatus.SIGNED) else it }
        }
    }

    override suspend fun save(
        pdfUri: Uri,
        pageUris: List<Uri>,
        title: String,
    ): Result<Document> = Result.failure(UnsupportedOperationException("Not used in tests"))
}
