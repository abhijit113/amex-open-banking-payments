package com.amex.payments.infrastructure.persistence.truelayer.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TrueLayerCreatePayoutRequest(
    @JsonProperty("amount_in_minor")
    val amountInMinor: Long,

    @JsonProperty("currency")
    val currency: String,

    @JsonProperty("reference")
    val reference: String,

    @JsonProperty("beneficiary")
    val beneficiary: TrueLayerBeneficiary
)

data class TrueLayerBeneficiary(
    @JsonProperty("account_holder_name")
    val accountHolderName: String,

    @JsonProperty("account_identifier")
    val accountIdentifier: TrueLayerAccountIdentifier
)

data class TrueLayerAccountIdentifier(
    @JsonProperty("type")
    val type: String = "sort_code_account_number",

    @JsonProperty("sort_code")
    val sortCode: String,

    @JsonProperty("account_number")
    val accountNumber: String
)