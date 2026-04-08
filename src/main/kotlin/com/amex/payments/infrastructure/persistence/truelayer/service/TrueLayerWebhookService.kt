package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.domain.model.PaymentStatus
import com.amex.payments.infrastructure.persistence.entity.PaymentIntentEventEntity
import com.amex.payments.infrastructure.persistence.repository.PaymentIntentEventRepository
import com.amex.payments.infrastructure.persistence.repository.PaymentIntentRepository
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerWebhookEvent
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.Instant

@ApplicationScoped
class TrueLayerWebhookService {
    @Inject
    lateinit var paymentIntentRepository: PaymentIntentRepository

    @Inject
    lateinit var paymentIntentEventRepository: PaymentIntentEventRepository

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Transactional
    fun handle(event: TrueLayerWebhookEvent) {
        if (paymentIntentEventRepository.existsByEventId(event.event_id)) {
            return
        }

        val externalPaymentId = event.payment_id ?: return

        val paymentIntent =
            paymentIntentRepository.findByExternalProviderPaymentId(externalPaymentId)
                ?: return

        val eventEntity =
            PaymentIntentEventEntity().apply {
                eventId = event.event_id
                paymentIntentId = paymentIntent.id
                externalProviderPaymentId = externalPaymentId
                eventType = event.event_type
                providerStatus = event.payment_status
                payload = objectMapper.writeValueAsString(event)
                createdAt = Instant.now()
            }

        paymentIntentEventRepository.persist(eventEntity)

        val mappedStatus =
            mapToInternalStatus(
                eventType = event.event_type,
                providerStatus = event.payment_status,
            )

        if (mappedStatus != null) {
            paymentIntent.status = mappedStatus.name
            paymentIntent.updatedAt = Instant.now()
        }
    }

    private fun mapToInternalStatus(
        eventType: String,
        providerStatus: String?,
    ): PaymentStatus? {
        return when {
            eventType.contains("authoriz", ignoreCase = true) ||
                providerStatus.equals("authorized", ignoreCase = true) ->
                PaymentStatus.AUTHORIZED

            eventType.contains("execut", ignoreCase = true) ||
                providerStatus.equals("executed", ignoreCase = true) ->
                PaymentStatus.EXECUTED

            eventType.contains("settl", ignoreCase = true) ||
                providerStatus.equals("settled", ignoreCase = true) ->
                PaymentStatus.SETTLED

            eventType.contains("fail", ignoreCase = true) ||
                providerStatus.equals("failed", ignoreCase = true) ->
                PaymentStatus.FAILED

            eventType.contains("expire", ignoreCase = true) ||
                providerStatus.equals("expired", ignoreCase = true) ->
                PaymentStatus.EXPIRED

            else -> null
        }
    }
}
