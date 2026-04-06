package com.amex.payments.infrastructure.persistence.truelayer.client

import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePaymentResponse
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/v3/payments")
@RegisterRestClient(configKey = "truelayer-payments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
interface TrueLayerPaymentsClient {
    @POST
    fun createPayment(
        @HeaderParam("Authorization") authorization: String,
        @HeaderParam("Idempotency-Key") idempotencyKey: String,
        @HeaderParam("Tl-Signature") signature: String,
        request: TrueLayerCreatePaymentRequest,
    ): TrueLayerCreatePaymentResponse
}
