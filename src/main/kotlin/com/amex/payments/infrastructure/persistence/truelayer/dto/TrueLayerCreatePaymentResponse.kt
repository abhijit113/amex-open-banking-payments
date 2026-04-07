package com.amex.payments.infrastructure.persistence.truelayer.dto

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class TrueLayerCreatePaymentResponse(
    val id: String,
    val status: String,
    val resource_token: String? = null,
    val authorization_flow: TrueLayerAuthorizationFlow? = null,
) {
    fun redirectUrl(returnUri: String): String? {
        authorization_flow?.url?.let { return it }

        if (resource_token == null) return null

        val encodedReturnUri = URLEncoder.encode(returnUri, StandardCharsets.UTF_8)
        return "https://payment.truelayer-sandbox.com/payments#payment_id=$id&resource_token=$resource_token&return_uri=$encodedReturnUri"
    }
}

data class TrueLayerAuthorizationFlow(
    val url: String? = null,
)
