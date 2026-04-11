package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.domain.model.PaymentStatus
import com.amex.payments.infrastructure.persistence.entity.PaymentIntentEntity
import com.amex.payments.infrastructure.persistence.repository.PaymentIntentEventRepository
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerWebhookEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.jboss.logging.Logger
import java.time.Instant

@ApplicationScoped
class TrueLayerWebhookService {
    @Inject
    lateinit var paymentIntentEventRepository: PaymentIntentEventRepository

    @Inject
    lateinit var entityManager: EntityManager

    companion object {
        private val LOG: Logger = Logger.getLogger(TrueLayerWebhookService::class.java)
    }

    @Transactional
    fun handle(event: TrueLayerWebhookEvent) {
        LOG.infof(
            "Handling TrueLayer webhook. eventId=%s eventType=%s paymentId=%s",
            event.eventId,
            event.eventType,
            event.paymentId,
        )

        if (paymentIntentEventRepository.existsByEventId(event.eventId)) {
            LOG.infof("Ignoring duplicate TrueLayer webhook eventId=%s", event.eventId)
            return
        }

        val paymentId = event.paymentId
        if (paymentId.isNullOrBlank()) {
            LOG.warnf("Ignoring webhook %s because paymentId is missing", event.eventId)
            return
        }

        val paymentIntent = findPaymentIntentByExternalProviderPaymentId(paymentId)
        if (paymentIntent == null) {
            LOG.warnf(
                "No payment intent found for webhook eventId=%s paymentId=%s",
                event.eventId,
                paymentId,
            )
            return
        }

        val newStatus =
            when (event.eventType) {
                "payment_executed" -> PaymentStatus.EXECUTED.name
                "payment_creditable" -> PaymentStatus.EXECUTED.name
                "payment_settled" -> PaymentStatus.SETTLED.name
                else -> {
                    LOG.infof(
                        "Ignoring unsupported TrueLayer event type. eventId=%s eventType=%s",
                        event.eventId,
                        event.eventType,
                    )
                    return
                }
            }

        paymentIntent.status = newStatus
        paymentIntent.updatedAt = Instant.now()

        LOG.infof(
            "Updated payment intent id=%s paymentId=%s to status=%s from webhook eventId=%s",
            paymentIntent.id,
            paymentId,
            newStatus,
            event.eventId,
        )
    }

    private fun findPaymentIntentByExternalProviderPaymentId(paymentId: String): PaymentIntentEntity? =
        entityManager.createQuery(
            """
            select p
            from PaymentIntentEntity p
            where p.externalProviderPaymentId = :paymentId
            """.trimIndent(),
            PaymentIntentEntity::class.java,
        )
            .setParameter("paymentId", paymentId)
            .resultList
            .firstOrNull()
}
