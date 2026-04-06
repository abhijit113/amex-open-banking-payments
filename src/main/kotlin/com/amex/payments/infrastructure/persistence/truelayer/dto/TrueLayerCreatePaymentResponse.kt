package com.amex.payments.infrastructure.persistence.truelayer.dto

data class TrueLayerCreatePaymentResponse(
    val id: String,
    val status: String,
    val authorization_flow: TrueLayerAuthorizationFlow?,
) {
    val authorizationFlowUrl: String?
        get() = authorization_flow?.url
}

data class TrueLayerAuthorizationFlow(
    val url: String,
)
