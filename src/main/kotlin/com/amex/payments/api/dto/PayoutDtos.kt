package org.example.com.amex.payments.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreatePayoutRequest(
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
    @field:NotBlank
    @field:Size(max = 140)
    val accountHolderName: String,
    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]{6}$|^[0-9]{2}-[0-9]{2}-[0-9]{2}$")
    val sortCode: String,
    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]{8}$")
    val accountNumber: String,
    @field:Pattern(regexp = "^$|^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
    val dateOfBirth: String? = null,
    val addressLine1: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val countryCode: String? = "GB",
)

data class PayoutResponse(
    val id: String,
    val traceId: String,
    val endToEndId: String,
    val amountInMinor: Long,
    val currency: String,
    val status: String,
    val provider: String,
    val externalProviderPayoutId: String?,
    val beneficiaryAccountHolderName: String,
    val beneficiarySortCode: String,
    val beneficiaryAccountNumberLast4: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
