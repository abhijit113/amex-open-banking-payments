package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerBeneficiary
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerPaymentMethod
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerProviderSelection
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerSchemeSelection
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerUser
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.example.com.amex.payments.api.dto.CreatePaymentIntentRequest
import java.util.UUID

@ApplicationScoped
class TrueLayerCreatePaymentRequestFactory {
    @ConfigProperty(name = "truelayer.merchant-account-id")
    lateinit var merchantAccountId: String

    @ConfigProperty(name = "truelayer.beneficiary-name")
    lateinit var beneficiaryName: String

    fun from(request: CreatePaymentIntentRequest): TrueLayerCreatePaymentRequest {
        return TrueLayerCreatePaymentRequest(
            amount_in_minor = request.amountInMinor,
            currency = request.currency.uppercase(),
            user =
                TrueLayerUser(
                    id = toUuidOrRandom(request.traceId),
                    name = "POC User",
                    email = "poc.user@example.com",
                    phone = "+447700900000",
                    date_of_birth = "1990-01-01",
                ),
            payment_method =
                TrueLayerPaymentMethod(
                    provider_selection =
                        TrueLayerProviderSelection(
                            type = "preselected",
                            provider_id = request.provider,
                            scheme_selection =
                                TrueLayerSchemeSelection(
                                    type = "instant_preferred",
                                    allow_remitter_fee = false,
                                ),
                        ),
                    beneficiary =
                        TrueLayerBeneficiary(
                            type = "merchant_account",
                            merchant_account_id = merchantAccountId,
                            account_holder_name = beneficiaryName,
                            reference = sanitizeReference(request.endToEndId),
                        ),
                ),
        )
    }

    private fun toUuidOrRandom(value: String): String =
        try {
            UUID.fromString(value).toString()
        } catch (_: IllegalArgumentException) {
            UUID.randomUUID().toString()
        }

    private fun sanitizeReference(value: String): String = value.replace(Regex("[^a-zA-Z0-9\\-:()\\.,'\\+ \\?/]+"), "-")
}
