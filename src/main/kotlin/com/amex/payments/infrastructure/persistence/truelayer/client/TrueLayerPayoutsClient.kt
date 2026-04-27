package com.amex.payments.infrastructure.persistence.truelayer.client

import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePayoutRequest
import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerCreatePayoutResponse
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/v3/payouts")
@RegisterRestClient(configKey = "truelayer-payments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
interface TrueLayerPayoutsClient {
    @POST
    fun createPayout(
        @HeaderParam("Authorization") authorization: String,
        @HeaderParam("Idempotency-Key") idempotencyKey: String,
        @HeaderParam("Tl-Signature") signature: String,
        request: TrueLayerCreatePayoutRequest,
    ): TrueLayerCreatePayoutResponse

    @GET
    @Path("/{id}")
    fun getPayout(
        @HeaderParam("Authorization") authorization: String,
        @PathParam("id") id: String,
    ): TrueLayerCreatePayoutResponse
}
