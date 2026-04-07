package com.amex.payments.infrastructure.persistence.truelayer.dto

data class TrueLayerWebhookEvent(
    val event_id: String,
    val event_type: String,
    val payment_id: String? = null,
    val payment_status: String? = null,
)
