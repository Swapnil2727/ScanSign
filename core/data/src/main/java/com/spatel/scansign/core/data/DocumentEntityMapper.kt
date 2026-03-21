package com.spatel.scansign.core.data

import com.spatel.scansign.core.database.DocumentEntity
import com.spatel.scansign.core.database.DocumentPageEntity
import com.spatel.scansign.core.model.Document
import com.spatel.scansign.core.model.DocumentPage
import com.spatel.scansign.core.model.DocumentStatus

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
