package com.spatel.scansign.ui.signer

import com.spatel.scansign.core.signing.KeystoreManager

/**
 * Test double for [KeystoreManager]. Tracks generated/deleted aliases in memory.
 * Android Keystore is unavailable in JVM unit tests — this avoids the dependency.
 */
class FakeKeystoreManager : KeystoreManager() {

    val generatedAliases = mutableSetOf<String>()

    override fun generateKeyPair(alias: String): java.security.KeyPair {
        generatedAliases.add(alias)
        // Return a real RSA key pair generated with the default provider (not Keystore)
        // so tests can run on JVM without Android hardware.
        val generator = java.security.KeyPairGenerator.getInstance("RSA")
        generator.initialize(512) // small key for test speed
        return generator.generateKeyPair()
    }

    override fun hasKey(alias: String) = alias in generatedAliases

    override fun deleteKey(alias: String) {
        generatedAliases.remove(alias)
    }
}
