package com.spatel.scansign.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "document_pages",
    foreignKeys = [ForeignKey(
        entity = DocumentEntity::class,
        parentColumns = ["id"],
        childColumns = ["document_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("document_id")],
)
data class DocumentPageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "page_number")  val pageNumber: Int,
    @ColumnInfo(name = "image_path")   val imagePath: String,
    val rotation: Int = 0,
)
