package com.amex.payments.api

import com.amex.payments.application.service.PaymentIntentService
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
import org.example.com.amex.payments.api.dto.CreatePaymentIntentRequest
import org.example.com.amex.payments.api.dto.PaymentIntentResponse

@Path("/payment-intents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Blocking
class PaymentIntentResource {
    @Inject
    lateinit var paymentIntentService: PaymentIntentService

    @POST
    fun create(
        @Valid request: CreatePaymentIntentRequest,
    ): PaymentIntentResponse = paymentIntentService.create(request)

    @GET
    @Path("/{id}")
    fun get(
        @PathParam("id") id: String,
    ): PaymentIntentResponse = paymentIntentService.get(id)
}
