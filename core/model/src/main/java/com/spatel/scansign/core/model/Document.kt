package com.spatel.scansign.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pageCount: Int,
    val fileSize: Long,
    val status: DocumentStatus,
    val thumbnailPath: String? = null,
    val pdfPath: String? = null,
    val isSynced: Boolean = false,
)

@Serializable
enum class DocumentStatus {
    SCANNED,
    DRAFT,
    PENDING,
    SIGNED,
}
