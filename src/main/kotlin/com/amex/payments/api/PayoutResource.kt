package com.amex.payments.api

import com.amex.payments.application.service.PayoutService
import io.smallrye.common.annotation.Blocking
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.example.com.amex.payments.api.dto.CreatePayoutRequest
import org.example.com.amex.payments.api.dto.PayoutResponse

@Path("/payouts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Blocking
class PayoutResource {
    @Inject
    lateinit var payoutService: PayoutService

    @POST
    fun create(
        @Valid request: CreatePayoutRequest,
    ): PayoutResponse = payoutService.create(request)

    @GET
    @Path("/{id}")
    fun get(
        @PathParam("id") id: String,
    ): PayoutResponse = payoutService.get(id)

    @GET
    @Path("/{id}/sync")
    fun sync(
        @PathParam("id") id: String,
    ): PayoutResponse = payoutService.syncStatus(id)
}
