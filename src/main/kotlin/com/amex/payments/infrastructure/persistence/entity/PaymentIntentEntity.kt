package com.amex.payments.infrastructure.persistence.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "payment_intent")
class PaymentIntentEntity : PanacheEntityBase {
    @Id
    lateinit var id: String

    @Column(name = "trace_id", nullable = false, length = 64)
    lateinit var traceId: String

    @Column(name = "end_to_end_id", nullable = false, length = 64)
    lateinit var endToEndId: String

    @Column(name = "amount_in_minor", nullable = false)
    var amountInMinor: Long = 0

    @Column(name = "currency", nullable = false, length = 3)
    lateinit var currency: String

    @Column(name = "status", nullable = false, length = 32)
    lateinit var status: String

    @Column(name = "provider", length = 32)
    var provider: String? = null

    @Column(name = "external_provider_payment_id", length = 128)
    var externalProviderPaymentId: String? = null

    @Column(name = "external_bank_trace_id", length = 128)
    var externalBankTraceId: String? = null

    @Column(name = "redirect_url")
    var redirectUrl: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
}
