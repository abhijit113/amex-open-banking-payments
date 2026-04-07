package com.amex.payments.infrastructure.persistence.repository

import com.amex.payments.infrastructure.persistence.entity.PaymentIntentEventEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PaymentIntentEventRepository : PanacheRepository<PaymentIntentEventEntity> {
    fun existsByEventId(eventId: String): Boolean = find("eventId", eventId).count() > 0
}
