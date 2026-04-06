package com.amex.payments.application.service

import com.amex.payments.domain.model.PaymentStatus
import com.amex.payments.infrastructure.persistence.entity.PaymentIntentEntity
import com.amex.payments.infrastructure.persistence.repository.PaymentIntentRepository
import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerCreatePaymentRequestFactory
import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerGateway
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import org.example.com.amex.payments.api.dto.CreatePaymentIntentRequest
import org.example.com.amex.payments.api.dto.PaymentIntentResponse
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class PaymentIntentService {
    @Inject
    lateinit var paymentIntentRepository: PaymentIntentRepository

    @Inject
    lateinit var trueLayerRequestFactory: TrueLayerCreatePaymentRequestFactory

    @Inject
    lateinit var trueLayerGateway: TrueLayerGateway

    @Transactional
    fun create(request: CreatePaymentIntentRequest): PaymentIntentResponse {
        val now = Instant.now()
        val paymentIntentId = UUID.randomUUID().toString()

        val entity =
            PaymentIntentEntity().apply {
                id = paymentIntentId
                traceId = request.traceId
                endToEndId = request.endToEndId
                amountInMinor = request.amountInMinor
                currency = request.currency.uppercase()
                status = PaymentStatus.CREATED.name
                provider = "TRUELAYER"
                createdAt = now
                updatedAt = now
            }

        paymentIntentRepository.persist(entity)

        val tlRequest = trueLayerRequestFactory.from(request)
        val tlResponse = trueLayerGateway.createPayment(tlRequest)

        entity.externalProviderPaymentId = tlResponse.id
        entity.redirectUrl = tlResponse.authorizationFlowUrl
        entity.status = PaymentStatus.AUTHORIZATION_REQUIRED.name
        entity.updatedAt = Instant.now()

        return entity.toResponse()
    }

    fun get(id: String): PaymentIntentResponse {
        val entity =
            paymentIntentRepository.findByIntentId(id)
                ?: throw NotFoundException("Payment intent not found for id=$id")

        return entity.toResponse()
    }

    private fun PaymentIntentEntity.toResponse(): PaymentIntentResponse =
        PaymentIntentResponse(
            id = id,
            traceId = traceId,
            endToEndId = endToEndId,
            amountInMinor = amountInMinor,
            currency = currency,
            status = status,
            provider = provider,
            externalProviderPaymentId = externalProviderPaymentId,
            externalBankTraceId = externalBankTraceId,
            redirectUrl = redirectUrl,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
