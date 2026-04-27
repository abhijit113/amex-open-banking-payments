package com.amex.payments.infrastructure.persistence.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "payout")
class PayoutEntity : PanacheEntityBase {
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

    @Column(name = "status", nullable = false, length = 64)
    lateinit var status: String

    @Column(name = "provider", nullable = false, length = 32)
    lateinit var provider: String

    @Column(name = "external_provider_payout_id", length = 128)
    var externalProviderPayoutId: String? = null

    @Column(name = "beneficiary_account_holder_name", nullable = false, length = 140)
    lateinit var beneficiaryAccountHolderName: String

    @Column(name = "beneficiary_sort_code", nullable = false, length = 16)
    lateinit var beneficiarySortCode: String

    @Column(name = "beneficiary_account_number_last4", nullable = false, length = 4)
    lateinit var beneficiaryAccountNumberLast4: String

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
}
