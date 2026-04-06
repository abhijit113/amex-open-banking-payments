package com.amex.payments.infrastructure.persistence.truelayer.client

import com.amex.payments.infrastructure.persistence.truelayer.dto.TrueLayerAccessTokenResponse
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/connect/token")
@RegisterRestClient(configKey = "truelayer-auth")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
interface TrueLayerAuthClient {
    @POST
    fun token(form: String): TrueLayerAccessTokenResponse
}
