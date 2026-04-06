package org.example.com.amex.payments.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePaymentIntentRequest(
    @field:NotBlank
    @field:Size(max = 64)
    val traceId: String,
    @field:NotBlank
    @field:Size(max = 64)
    val endToEndId: String,
    @field:Min(1)
    val amountInMinor: Long,
    @field:NotBlank
    @field:Size(min = 3, max = 3)
    val currency: String,
    @field:Size(max = 32)
    val provider: String? = "TRUELAYER",
)
