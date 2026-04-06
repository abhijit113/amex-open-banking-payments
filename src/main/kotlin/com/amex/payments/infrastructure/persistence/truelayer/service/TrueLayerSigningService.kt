package com.amex.payments.infrastructure.persistence.truelayer.service

import com.truelayer.signing.Signer
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

@ApplicationScoped
class TrueLayerSigningService {
    @ConfigProperty(name = "truelayer.signing-key-id")
    lateinit var keyId: String

    @ConfigProperty(name = "truelayer.private-key-path")
    lateinit var privateKeyPath: String

    fun sign(
        method: String,
        path: String,
        body: String,
        idempotencyKey: String,
    ): String {
        val privateKey = loadPrivateKey(privateKeyPath)

        return Signer.from(keyId, privateKey)
            .header("Idempotency-Key", idempotencyKey)
            .method(method.lowercase())
            .path(path)
            .body(body)
            .sign()
    }

    private fun loadPrivateKey(path: String): ECPrivateKey {
        val pem = Files.readString(Path.of(path))
        val der =
            when {
                pem.contains("-----BEGIN PRIVATE KEY-----") ->
                    pemToDer(
                        pem,
                        "-----BEGIN PRIVATE KEY-----",
                        "-----END PRIVATE KEY-----",
                    )
                pem.contains("-----BEGIN EC PRIVATE KEY-----") -> {
                    val sec1 =
                        pemToDer(
                            pem,
                            "-----BEGIN EC PRIVATE KEY-----",
                            "-----END EC PRIVATE KEY-----",
                        )
                    wrapSec1EcKeyInPkcs8(sec1)
                }
                else -> error("Unsupported private key format")
            }

        return KeyFactory.getInstance("EC")
            .generatePrivate(PKCS8EncodedKeySpec(der)) as ECPrivateKey
    }

    private fun pemToDer(
        pem: String,
        begin: String,
        end: String,
    ): ByteArray {
        val base64 = pem.replace(begin, "").replace(end, "").replace("\\s".toRegex(), "")
        return Base64.getDecoder().decode(base64)
    }

    private fun wrapSec1EcKeyInPkcs8(sec1Der: ByteArray): ByteArray {
        val version = byteArrayOf(0x02, 0x01, 0x00)
        val algorithmIdentifier =
            byteArrayOf(
                0x30, 0x10,
                0x06, 0x07, 0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x02, 0x01,
                0x06, 0x05, 0x2B, 0x81.toByte(), 0x04, 0x00, 0x23,
            )
        val privateKeyOctetString = byteArrayOf(0x04) + encodeLength(sec1Der.size) + sec1Der
        val body = version + algorithmIdentifier + privateKeyOctetString
        return byteArrayOf(0x30) + encodeLength(body.size) + body
    }

    private fun encodeLength(length: Int): ByteArray =
        when {
            length < 0x80 -> byteArrayOf(length.toByte())
            length <= 0xFF -> byteArrayOf(0x81.toByte(), length.toByte())
            else -> byteArrayOf(0x82.toByte(), (length shr 8).toByte(), length.toByte())
        }
}
