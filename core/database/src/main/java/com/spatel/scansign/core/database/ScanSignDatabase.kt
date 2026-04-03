package com.spatel.scansign.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DocumentEntity::class, DocumentPageEntity::class, SignatureEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class ScanSignDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun signatureDao(): SignatureDao
}
