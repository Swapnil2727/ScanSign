package com.spatel.scansign.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DocumentPage(
    val id: String,
    val documentId: String,
    val pageNumber: Int,
    val imagePath: String,
    val rotation: Int = 0,
)
