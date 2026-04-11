package com.spatel.scansign.util

import com.spatel.scansign.core.data.SignatureRepository
import com.spatel.scansign.core.model.Signature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSignatureRepository : SignatureRepository {

    private val _signatures = MutableStateFlow<List<Signature>>(emptyList())

    override suspend fun save(signature: Signature): Result<Unit> {
        _signatures.update { it + signature }
        return Result.success(Unit)
    }

    override fun getAll(): Flow<List<Signature>> = _signatures

    override suspend fun delete(id: String) {
        _signatures.update { it.filter { sig -> sig.id != id } }
    }
}
