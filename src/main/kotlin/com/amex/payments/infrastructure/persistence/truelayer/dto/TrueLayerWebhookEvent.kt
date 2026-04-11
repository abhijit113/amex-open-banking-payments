package com.amex.payments.infrastructure.persistence.truelayer.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrueLayerWebhookEvent(
    @JsonProperty("event_id")
    val eventId: String,
    @JsonProperty("type")
    val eventType: String,
    @JsonProperty("payment_id")
    val paymentId: String? = null,
)
