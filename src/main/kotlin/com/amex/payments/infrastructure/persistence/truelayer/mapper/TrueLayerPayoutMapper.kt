package com.amex.payments.infrastructure.persistence.truelayer.mapper

import org.example.com.amex.payments.api.dto.CreatePayoutRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerAccountIdentifier
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerBeneficiary
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePayoutRequest

fun CreatePayoutRequest.toTrueLayerRequest(): TrueLayerCreatePayoutRequest =
    TrueLayerCreatePayoutRequest(
        amountInMinor = amountInMinor,
        currency = currency,
        reference = endToEndId,
        beneficiary = TrueLayerBeneficiary(
            accountHolderName = accountHolderName,
            accountIdentifier = TrueLayerAccountIdentifier(
                sortCode = sortCode.replace("-", ""),
                accountNumber = accountNumber
            )
        )
    )