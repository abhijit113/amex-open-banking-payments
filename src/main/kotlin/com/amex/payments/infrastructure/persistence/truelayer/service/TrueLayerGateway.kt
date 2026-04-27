package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.infrastructure.persistence.truelayer.client.TrueLayerPaymentsClient
import com.amex.payments.infrastructure.persistence.truelayer.client.TrueLayerPayoutsClient
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentResponse
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePayoutRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePayoutResponse
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
    @RestClient
    lateinit var payoutsClient: TrueLayerPayoutsClient

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
                path = TRUE_LAYER_PAYMENTS_PATH,
                body = body,
                idempotencyKey = idempotencyKey,
            )

        try {
            return paymentsClient.createPayment(
                authorization = bearer(accessToken),
                idempotencyKey = idempotencyKey,
                signature = signature,
                request = request,
            )
        } catch (e: WebApplicationException) {
            logTrueLayerError(
                label = "PAYMENT",
                exception = e,
                requestBody = body,
            )
            throw e
        }
    }

    fun createPayout(request: TrueLayerCreatePayoutRequest): TrueLayerCreatePayoutResponse {
        val accessToken = tokenService.getAccessToken()
        val body = objectMapper.writeValueAsString(request)
        val idempotencyKey = UUID.randomUUID().toString()

        val signature =
            signingService.sign(
                method = "POST",
                path = TRUE_LAYER_PAYOUTS_PATH,
                body = body,
                idempotencyKey = idempotencyKey,
            )

        try {
            return payoutsClient.createPayout(
                authorization = bearer(accessToken),
                idempotencyKey = idempotencyKey,
                signature = signature,
                request = request,
            )
        } catch (e: WebApplicationException) {
            logTrueLayerError(
                label = "PAYOUT",
                exception = e,
                requestBody = body,
            )
            throw e
        }
    }

    fun getPayout(id: String): TrueLayerCreatePayoutResponse {
        val accessToken = tokenService.getAccessToken()

        try {
            return payoutsClient.getPayout(
                authorization = bearer(accessToken),
                id = id,
            )
        } catch (e: WebApplicationException) {
            logTrueLayerError(
                label = "GET PAYOUT",
                exception = e,
                requestBody = null,
            )
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
                path = TRUE_LAYER_TEST_SIGNATURE_PATH,
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

    private fun bearer(accessToken: String): String = "Bearer $accessToken"

    private fun logTrueLayerError(
        label: String,
        exception: WebApplicationException,
        requestBody: String?,
    ) {
        val responseBody =
            try {
                exception.response?.readEntity(String::class.java)
            } catch (_: Exception) {
                null
            }

        println("===== TL $label ERROR START =====")
        println("STATUS=${exception.response?.status}")
        if (requestBody != null) {
            println("REQUEST_BODY=$requestBody")
        }
        println("RESPONSE_BODY=$responseBody")
        println("===== TL $label ERROR END =====")
    }

    companion object {
        private const val TRUE_LAYER_PAYMENTS_PATH = "/v3/payments"
        private const val TRUE_LAYER_PAYOUTS_PATH = "/v3/payouts"
        private const val TRUE_LAYER_TEST_SIGNATURE_PATH = "/test-signature"
    }
}
