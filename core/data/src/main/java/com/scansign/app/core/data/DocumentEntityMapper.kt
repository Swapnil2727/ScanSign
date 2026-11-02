package com.scansign.app.core.data

import com.scansign.app.core.database.DocumentEntity
import com.scansign.app.core.database.DocumentPageEntity
import com.scansign.app.core.model.Document
import com.scansign.app.core.model.DocumentPage
import com.scansign.app.core.model.DocumentStatus

internal fun DocumentEntity.toDomain(): Document = Document(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pageCount = pageCount,
    fileSize = fileSizeBytes,
    status = DocumentStatus.valueOf(status),
    thumbnailPath = thumbnailPath,
    pdfPath = pdfPath,
)

internal fun DocumentPageEntity.toDomain(): DocumentPage = DocumentPage(
    id = id,
    documentId = documentId,
    pageNumber = pageNumber,
    imagePath = imagePath,
    rotation = rotation,
)
