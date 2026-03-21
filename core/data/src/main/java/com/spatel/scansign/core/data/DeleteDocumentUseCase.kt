package com.spatel.scansign.core.data

fun interface DeleteDocumentUseCase {

    suspend operator fun invoke(id: String)

    companion object {
        operator fun invoke(repository: DocumentRepository): DeleteDocumentUseCase =
            DeleteDocumentUseCase { id -> repository.delete(id) }
    }
}
