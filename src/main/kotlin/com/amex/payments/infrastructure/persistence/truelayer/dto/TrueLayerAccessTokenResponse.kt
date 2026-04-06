package com.amex.payments.infrastructure.persistence.truelayer.dto

data class TrueLayerAccessTokenResponse(
    val access_token: String,
    val expires_in: Long,
    val token_type: String,
    val scope: String,
)
