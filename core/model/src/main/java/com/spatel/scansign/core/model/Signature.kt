package com.spatel.scansign.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Signature(
    val id: String,
    val name: String,
    val createdAt: Long,
    val type: SignatureType,
    val bitmapPath: String? = null,        // DRAWN and IMAGE: path to saved PNG on disk
    val certificateAlias: String? = null,  // DIGITAL: Android Keystore alias
)

@Serializable
enum class SignatureType {
    DRAWN,
    IMAGE,
    DIGITAL,
}
