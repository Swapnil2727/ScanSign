package com.spatel.scansign.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    indices = [Index("created_at"), Index("title")],
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "pdf_path")        val pdfPath: String?,
    @ColumnInfo(name = "thumbnail_path")  val thumbnailPath: String?,
    @ColumnInfo(name = "page_count")      val pageCount: Int,
    @ColumnInfo(name = "file_size_bytes") val fileSizeBytes: Long,
    val status: String,                   // stores DocumentStatus.name
    @ColumnInfo(name = "created_at")      val createdAt: Long,
    @ColumnInfo(name = "updated_at")      val updatedAt: Long,
)
