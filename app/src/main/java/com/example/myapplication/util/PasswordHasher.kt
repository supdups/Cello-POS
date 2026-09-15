package com.example.myapplication.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Simple salted SHA-256 hashing. Good enough for a local cashier app;
 * if this ever talks to a real backend, use that backend's auth instead
 * of shipping passwords through Room directly.
 */
object PasswordHasher {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray())
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }
}
