package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.infrastructure.persistence.truelayer.client.TrueLayerPaymentsClient
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.UUID

@ApplicationScoped
class TrueLayerGateway {
    @Inject
    @RestClient
    lateinit var paymentsClient: TrueLayerPaymentsClient

    @Inject
    lateinit var tokenService: TrueLayerTokenService

    @Inject
    lateinit var signingService: TrueLayerSigningService

    @Inject
    lateinit var objectMapper: ObjectMapper

    fun createPayment(request: TrueLayerCreatePaymentRequest): TrueLayerCreatePaymentResponse {
        val accessToken = tokenService.getAccessToken()
        val body = objectMapper.writeValueAsString(request)
        val idempotencyKey = UUID.randomUUID().toString()

        val signature =
            signingService.sign(
                method = "POST",
                path = "/v3/payments",
                body = body,
                idempotencyKey = idempotencyKey,
            )

        try {
            return paymentsClient.createPayment(
                authorization = "Bearer $accessToken",
                idempotencyKey = idempotencyKey,
                signature = signature,
                request = request,
            )
        } catch (e: WebApplicationException) {
            val responseBody = e.response?.readEntity(String::class.java)

            println("===== TL PAYMENT ERROR START =====")
            println("STATUS=${e.response?.status}")
            println("REQUEST_BODY=$body")
            println("RESPONSE_BODY=$responseBody")
            println("===== TL PAYMENT ERROR END =====")

            throw e
        }
    }

    fun debugSignatureForTestEndpoint(
        body: String = """{"nonce":"9f952b2e-1675-4be8-bb39-6f4343803c2f"}""",
        idempotencyKey: String = "11111111-1111-4111-8111-111111111111",
    ): Triple<String, String, String> {
        val signature =
            signingService.sign(
                method = "POST",
                path = "/test-signature",
                body = body,
                idempotencyKey = idempotencyKey,
            )

        println("===== TL TEST SIGNATURE START =====")
        println("TL_SIGNATURE=$signature")
        println("TL_BODY=$body")
        println("TL_IDEMPOTENCY_KEY=$idempotencyKey")
        println("===== TL TEST SIGNATURE END =====")

        return Triple(signature, body, idempotencyKey)
    }
}
