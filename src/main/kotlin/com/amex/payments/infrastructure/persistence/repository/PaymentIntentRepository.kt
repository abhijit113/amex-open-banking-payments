package com.amex.payments.infrastructure.persistence.repository

import com.amex.payments.infrastructure.persistence.entity.PaymentIntentEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PaymentIntentRepository : PanacheRepository<PaymentIntentEntity> {
    fun findByIntentId(id: String): PaymentIntentEntity? = find("id", id).firstResult()

    fun findByExternalProviderPaymentId(externalProviderPaymentId: String): PaymentIntentEntity? =
        find("externalProviderPaymentId", externalProviderPaymentId).firstResult()
}
