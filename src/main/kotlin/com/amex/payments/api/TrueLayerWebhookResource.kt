package com.amex.payments.api

import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerWebhookEvent
import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerWebhookService
import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerWebhookVerificationService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo

@Path("/webhooks/truelayer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TrueLayerWebhookResource {
    @Inject
    lateinit var webhookService: TrueLayerWebhookService

    @Inject
    lateinit var verificationService: TrueLayerWebhookVerificationService

    @Inject
    lateinit var objectMapper: ObjectMapper

    @POST
    fun receive(
        rawBody: String,
        @Context httpHeaders: HttpHeaders,
        @Context uriInfo: UriInfo,
    ): Response {
        val tlSignature =
            httpHeaders.getHeaderString("Tl-Signature")
                ?: return Response.status(Response.Status.UNAUTHORIZED).build()

        val headersMap: Map<String, String> =
            httpHeaders.requestHeaders
                .entries
                .associate { (key, values) -> key to values.joinToString(",") }

        verificationService.verify(
            tlSignature = tlSignature,
            path = "/${uriInfo.path}",
            headers = headersMap,
            rawBody = rawBody,
        )

        val event = objectMapper.readValue(rawBody, TrueLayerWebhookEvent::class.java)
        webhookService.handle(event)

        return Response.ok().build()
    }
}
