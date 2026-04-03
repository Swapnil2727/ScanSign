package com.spatel.scansign.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signatures")
data class SignatureEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,                                      // stores SignatureType.name
    @ColumnInfo(name = "bitmap_path") val bitmapPath: String?,   // DRAWN / IMAGE
    @ColumnInfo(name = "keystore_alias") val keystoreAlias: String?, // DIGITAL
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
