package com.amex.payments.infrastructure.persistence.truelayer.dto

data class TrueLayerCreatePaymentRequest(
    val amount_in_minor: Long,
    val currency: String,
    val user: TrueLayerUser,
    val payment_method: TrueLayerPaymentMethod,
    val hosted_page: TrueLayerHostedPage,
)

data class TrueLayerHostedPage(
    val return_uri: String,
)

data class TrueLayerUser(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val date_of_birth: String,
)

data class TrueLayerPaymentMethod(
    val type: String = "bank_transfer",
    val provider_selection: TrueLayerProviderSelection,
    val beneficiary: TrueLayerBeneficiary,
)

data class TrueLayerProviderSelection(
    val type: String = "preselected",
    val provider_id: String?,
    val scheme_selection: TrueLayerSchemeSelection? = null,
)

data class TrueLayerSchemeSelection(
    val type: String = "instant_preferred",
    val allow_remitter_fee: Boolean = false,
)

data class TrueLayerBeneficiary(
    val type: String = "merchant_account",
    val merchant_account_id: String,
    val account_holder_name: String? = null,
    val reference: String,
)
