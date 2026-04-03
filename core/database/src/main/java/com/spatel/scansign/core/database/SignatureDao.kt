package com.spatel.scansign.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signature: SignatureEntity)

    @Query("SELECT * FROM signatures ORDER BY created_at DESC")
    fun getAll(): Flow<List<SignatureEntity>>

    @Query("SELECT * FROM signatures WHERE id = :id")
    suspend fun getById(id: String): SignatureEntity?

    @Query("DELETE FROM signatures WHERE id = :id")
    suspend fun delete(id: String)
}
