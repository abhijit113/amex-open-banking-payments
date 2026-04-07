package com.amex.payments.infrastructure.persistence.truelayer.service

import com.truelayer.signing.Verifier
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.ForbiddenException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@ApplicationScoped
class TrueLayerWebhookVerificationService {
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    fun verify(
        tlSignature: String,
        path: String,
        headers: Map<String, String>,
        rawBody: String,
    ) {
        val jku = Verifier.extractJku(tlSignature)
        ensureSandboxJkuAllowed(jku)
        val jwks = fetchJwks(jku)

        Verifier.verifyWithJwks(jwks)
            .method("POST")
            .path(path)
            .headers(headers.filterKeys { !it.equals("Tl-Signature", ignoreCase = true) })
            .body(rawBody)
            .verify(tlSignature)
    }

    private fun ensureSandboxJkuAllowed(jku: String) {
        val allowed = "https://webhooks.truelayer-sandbox.com/.well-known/jwks"
        if (jku != allowed) {
            throw ForbiddenException("Untrusted JKU: $jku")
        }
    }

    private fun fetchJwks(jku: String): String {
        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(jku))
                .GET()
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw ForbiddenException("Failed to fetch JWKS from $jku")
        }
        return response.body()
    }
}
