package com.amex.payments.api

import com.amex.payments.application.service.TrueLayerWebhookService
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerWebhookEvent
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/webhooks/truelayer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TrueLayerWebhookResource {
    @Inject
    lateinit var webhookService: TrueLayerWebhookService

    @POST
    fun receive(event: TrueLayerWebhookEvent): Response {
        webhookService.handle(event)
        return Response.ok().build()
    }
}
