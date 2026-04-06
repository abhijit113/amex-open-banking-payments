package com.amex.payments.infrastructure.persistence.truelayer.service

import com.amex.payments.infrastructure.persistence.truelayer.client.TrueLayerAuthClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ApplicationScoped
class TrueLayerTokenService {
    @Inject
    @RestClient
    lateinit var authClient: TrueLayerAuthClient

    @ConfigProperty(name = "truelayer.client-id")
    lateinit var clientId: String

    @ConfigProperty(name = "truelayer.client-secret")
    lateinit var clientSecret: String

    fun getAccessToken(): String {
        val form =
            listOf(
                "grant_type" to "client_credentials",
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "scope" to "payments",
            ).joinToString("&") { "${it.first}=${encode(it.second)}" }

        return authClient.token(form).access_token
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
