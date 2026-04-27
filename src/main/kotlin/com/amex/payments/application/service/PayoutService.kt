package com.amex.payments.application.service

import com.amex.payments.infrastructure.persistence.entity.PayoutEntity
import com.amex.payments.infrastructure.persistence.repository.PayoutRepository
import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerCreatePayoutRequestFactory
import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerGateway
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import org.example.com.amex.payments.api.dto.CreatePayoutRequest
import org.example.com.amex.payments.api.dto.PayoutResponse
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class PayoutService {
    @Inject
    lateinit var payoutRepository: PayoutRepository

    @Inject
    lateinit var trueLayerCreatePayoutRequestFactory: TrueLayerCreatePayoutRequestFactory

    @Inject
    lateinit var trueLayerGateway: TrueLayerGateway

    @Transactional
    fun create(request: CreatePayoutRequest): PayoutResponse {
        val now = Instant.now()
        val payoutId = UUID.randomUUID().toString()

        val entity =
            PayoutEntity().apply {
                id = payoutId
                traceId = request.traceId
                endToEndId = request.endToEndId
                amountInMinor = request.amountInMinor
                currency = request.currency.uppercase()
                status = "CREATED"
                provider = "TRUELAYER"
                beneficiaryAccountHolderName = request.accountHolderName
                beneficiarySortCode = request.sortCode
                beneficiaryAccountNumberLast4 = request.accountNumber.takeLast(4)
                createdAt = now
                updatedAt = now
            }

        payoutRepository.persist(entity)

        val trueLayerRequest = trueLayerCreatePayoutRequestFactory.from(request)
        val trueLayerResponse = trueLayerGateway.createPayout(trueLayerRequest)

        entity.externalProviderPayoutId = trueLayerResponse.id
        entity.status = trueLayerResponse.status?.uppercase() ?: "PENDING"
        entity.updatedAt = Instant.now()

        return entity.toResponse()
    }

    fun get(id: String): PayoutResponse {
        val entity =
            payoutRepository.findByPayoutId(id)
                ?: throw NotFoundException("Payout not found for id=$id")

        return entity.toResponse()
    }

    @Transactional
    fun syncStatus(id: String): PayoutResponse {
        val entity =
            payoutRepository.findByPayoutId(id)
                ?: throw NotFoundException("Payout not found for id=$id")

        val externalPayoutId =
            entity.externalProviderPayoutId
                ?: throw NotFoundException("External TrueLayer payout id not found for payout id=$id")

        val trueLayerResponse = trueLayerGateway.getPayout(externalPayoutId)

        entity.status = trueLayerResponse.status?.uppercase() ?: entity.status
        entity.updatedAt = Instant.now()

        return entity.toResponse()
    }

    private fun PayoutEntity.toResponse(): PayoutResponse =
        PayoutResponse(
            id = id,
            traceId = traceId,
            endToEndId = endToEndId,
            amountInMinor = amountInMinor,
            currency = currency,
            status = status,
            provider = provider,
            externalProviderPayoutId = externalProviderPayoutId,
            beneficiaryAccountHolderName = beneficiaryAccountHolderName,
            beneficiarySortCode = beneficiarySortCode,
            beneficiaryAccountNumberLast4 = beneficiaryAccountNumberLast4,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
