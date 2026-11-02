package com.scansign.app.core.data

import com.scansign.app.core.database.SignatureDao
import com.scansign.app.core.database.SignatureEntity
import com.scansign.app.core.model.Signature
import com.scansign.app.core.model.SignatureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    override suspend fun delete(id: String) = dao.delete(id)
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
