package com.amex.payments.domain.model

enum class PaymentStatus {
    CREATED,
    PROVIDER_CREATED,
    AUTHORIZATION_REQUIRED,
    AUTHORIZED,
    EXECUTED,
    SETTLED,
    FAILED,
    EXPIRED,
}
