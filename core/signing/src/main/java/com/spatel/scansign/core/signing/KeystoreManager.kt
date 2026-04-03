package com.spatel.scansign.core.signing

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import javax.security.auth.x500.X500Principal

/**
 * Wraps Android Keystore operations for RSA key pairs used in digital signatures.
 *
 * Keys are stored inside the hardware-backed Keystore — private key material never
 * leaves secure hardware. Each key pair is identified by a unique [alias].
 */
class KeystoreManager {

    private val keyStore: KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    /**
     * Generates a new RSA-2048 key pair and stores it under [alias].
     * If a key already exists for this alias it is replaced.
     */
    fun generateKeyPair(alias: String): KeyPair {
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        )
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(X500Principal("CN=$alias, O=ScanSign"))
            .build()

        return KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_RSA, PROVIDER)
            .apply { initialize(spec) }
            .generateKeyPair()
    }

    /** Returns the [KeyPair] for [alias], or null if no key exists. */
    fun getKeyPair(alias: String): KeyPair? {
        if (!keyStore.containsAlias(alias)) return null
        val privateKey = keyStore.getKey(alias, null) as? PrivateKey ?: return null
        val publicKey = keyStore.getCertificate(alias)?.publicKey ?: return null
        return KeyPair(publicKey, privateKey)
    }

    /** Returns true if a key exists for [alias]. */
    fun hasKey(alias: String): Boolean = keyStore.containsAlias(alias)

    /** Permanently deletes the key pair for [alias]. No-op if the alias does not exist. */
    fun deleteKey(alias: String) {
        if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
    }

    companion object {
        private const val PROVIDER = "AndroidKeyStore"
    }
}
