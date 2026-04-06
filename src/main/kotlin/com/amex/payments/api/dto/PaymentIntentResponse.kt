package org.example.com.amex.payments.api.dto

import java.time.Instant

data class PaymentIntentResponse(
    val id: String,
    val traceId: String,
    val endToEndId: String,
    val amountInMinor: Long,
    val currency: String,
    val status: String,
    val provider: String?,
    val externalProviderPaymentId: String?,
    val externalBankTraceId: String?,
    val redirectUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
