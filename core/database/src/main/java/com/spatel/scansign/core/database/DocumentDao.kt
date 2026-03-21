package com.spatel.scansign.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<DocumentPageEntity>)

    @Query("SELECT * FROM documents ORDER BY created_at DESC")
    fun getAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Query("SELECT * FROM document_pages WHERE document_id = :documentId ORDER BY page_number ASC")
    suspend fun getPagesByDocumentId(documentId: String): List<DocumentPageEntity>

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE documents SET title = :title, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long)
}
