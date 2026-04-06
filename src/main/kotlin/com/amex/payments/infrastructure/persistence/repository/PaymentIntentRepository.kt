package com.amex.payments.infrastructure.persistence.repository

import com.amex.payments.infrastructure.persistence.entity.PaymentIntentEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PaymentIntentRepository : PanacheRepository<PaymentIntentEntity> {
    fun findByIntentId(id: String): PaymentIntentEntity? = find("id", id).firstResult()

    fun findByEndToEndId(endToEndId: String): PaymentIntentEntity? = find("endToEndId", endToEndId).firstResult()
}
