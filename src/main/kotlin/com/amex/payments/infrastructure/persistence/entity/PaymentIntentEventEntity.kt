package com.amex.payments.infrastructure.persistence.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "payment_intent_event")
class PaymentIntentEventEntity : PanacheEntityBase {
    @Id
    @Column(name = "event_id", nullable = false, length = 128)
    lateinit var eventId: String

    @Column(name = "payment_intent_id", nullable = false, length = 64)
    lateinit var paymentIntentId: String

    @Column(name = "external_provider_payment_id", nullable = false, length = 128)
    lateinit var externalProviderPaymentId: String

    @Column(name = "event_type", nullable = false, length = 64)
    lateinit var eventType: String

    @Column(name = "provider_status", length = 64)
    var providerStatus: String? = null

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    lateinit var payload: String

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant
}
