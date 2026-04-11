package com.spatel.scansign

import com.spatel.scansign.core.data.SignatureRepository
import com.spatel.scansign.core.model.Signature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake for [SignatureRepository]. Used in instrumented tests.
 */
class FakeSignatureRepository : SignatureRepository {

    private val _signatures = MutableStateFlow<List<Signature>>(emptyList())

    override suspend fun save(signature: Signature): Result<Unit> = runCatching {
        _signatures.update { it + signature }
    }

    override fun getAll(): Flow<List<Signature>> = _signatures

    override suspend fun delete(id: String) {
        _signatures.update { sigs -> sigs.filter { it.id != id } }
    }
}
