package com.amex.payments.infrastructure.persistence.repository

import com.amex.payments.infrastructure.persistence.entity.PayoutEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PayoutRepository : PanacheRepository<PayoutEntity> {
    fun findByPayoutId(id: String): PayoutEntity? = find("id", id).firstResult()

    fun findByEndToEndId(endToEndId: String): PayoutEntity? = find("endToEndId", endToEndId).firstResult()

    fun findByExternalProviderPayoutId(externalProviderPayoutId: String): PayoutEntity? =
        find("externalProviderPayoutId", externalProviderPayoutId).firstResult()
}
