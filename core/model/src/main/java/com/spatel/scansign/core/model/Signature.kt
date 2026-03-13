package com.spatel.scansign.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Signature(
    val id: String,
    val name: String,
    val createdAt: Long,
    val certificateAlias: String? = null,
    val type: SignatureType,
)

@Serializable
enum class SignatureType {
    DRAWN,
    IMAGE,
    DIGITAL,
}
