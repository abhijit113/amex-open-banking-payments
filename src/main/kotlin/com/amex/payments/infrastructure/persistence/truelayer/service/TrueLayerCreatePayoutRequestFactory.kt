package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePayoutRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerPayoutAccountIdentifier
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerPayoutAddress
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerPayoutBeneficiary
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.example.com.amex.payments.api.dto.CreatePayoutRequest

@ApplicationScoped
class TrueLayerCreatePayoutRequestFactory {
    @ConfigProperty(name = "truelayer.merchant-account-id")
    lateinit var merchantAccountId: String

    fun from(request: CreatePayoutRequest): TrueLayerCreatePayoutRequest =
        TrueLayerCreatePayoutRequest(
            merchant_account_id = merchantAccountId,
            amount_in_minor = request.amountInMinor,
            currency = request.currency.uppercase(),
            beneficiary =
                TrueLayerPayoutBeneficiary(
                    reference = sanitizeReference(request.endToEndId),
                    account_holder_name = request.accountHolderName,
                    account_identifier =
                        TrueLayerPayoutAccountIdentifier(
                            sort_code = normalizeSortCode(request.sortCode),
                            account_number = request.accountNumber,
                        ),
                    date_of_birth = request.dateOfBirth,
                    address = buildAddressOrNull(request),
                ),
            metadata =
                mapOf(
                    "trace_id" to request.traceId,
                    "end_to_end_id" to request.endToEndId,
                    "source" to "amex-open-banking-payments-poc",
                ),
        )

    private fun buildAddressOrNull(request: CreatePayoutRequest): TrueLayerPayoutAddress? {
        val addressLine1 = request.addressLine1
        val city = request.city
        val zip = request.zip

        return if (!addressLine1.isNullOrBlank() && !city.isNullOrBlank() && !zip.isNullOrBlank()) {
            TrueLayerPayoutAddress(
                address_line1 = addressLine1,
                city = city,
                zip = zip,
                country_code = request.countryCode ?: "GB",
            )
        } else {
            null
        }
    }

    private fun normalizeSortCode(sortCode: String): String = sortCode.replace("-", "").replace(" ", "")

    private fun sanitizeReference(value: String): String = value.replace(Regex("[^a-zA-Z0-9\\-:()\\.,'\\+ \\?/]+"), "-")
}
