package com.spatel.scansign.core.data

import com.spatel.scansign.core.database.SignatureDao
import com.spatel.scansign.core.database.SignatureEntity
import com.spatel.scansign.core.model.Signature
import com.spatel.scansign.core.model.SignatureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

interface SignatureRepository {

    suspend fun save(signature: Signature): Result<Unit>

    fun getAll(): Flow<List<Signature>>

    suspend fun delete(id: String)

    companion object {
        operator fun invoke(dao: SignatureDao): SignatureRepository =
            SignatureRepositoryImpl(dao)
    }
}

internal class SignatureRepositoryImpl(private val dao: SignatureDao) : SignatureRepository {

    override suspend fun save(signature: Signature): Result<Unit> = runCatching {
        dao.insert(signature.toEntity())
    }

    override fun getAll(): Flow<List<Signature>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun delete(id: String) {
        // Delete associated bitmap file first (if exists)
        runCatching {
            dao.getById(id)?.let { entity ->
                entity.bitmapPath?.let { path ->
                    File(path).delete()
                }
            }
        }
        // Then delete database entry
        dao.delete(id)
    }
}

private fun Signature.toEntity() = SignatureEntity(
    id = id,
    name = name,
    type = type.name,
    bitmapPath = bitmapPath,
    keystoreAlias = certificateAlias,
    createdAt = createdAt,
)

private fun SignatureEntity.toDomain() = Signature(
    id = id,
    name = name,
    type = SignatureType.valueOf(type),
    bitmapPath = bitmapPath,
    certificateAlias = keystoreAlias,
    createdAt = createdAt,
)
