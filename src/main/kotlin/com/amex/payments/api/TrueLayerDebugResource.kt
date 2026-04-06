package com.amex.payments.api

import com.amex.payments.infrastructure.persistence.truelayer.service.TrueLayerGateway
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/debug/truelayer")
class TrueLayerDebugResource {
    @Inject
    lateinit var gateway: TrueLayerGateway

    @GET
    @Path("/signature")
    @Produces(MediaType.APPLICATION_JSON)
    fun generateSignature(): Map<String, String> {
        val (signature, body, idempotencyKey) = gateway.debugSignatureForTestEndpoint()

        return mapOf(
            "signature" to signature,
            "body" to body,
            "idempotencyKey" to idempotencyKey,
        )
    }
}
