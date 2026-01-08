package no.nav.hm.finnhjelpemiddelbff.auth

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable

@Client("\${azure.introspection.endpoint}")
interface AzureAdUserClient {
    @Post(value = "/", produces = [MediaType.APPLICATION_FORM_URLENCODED], consumes = ["application/json"])
    @SingleResult
    suspend fun validateToken(@Body authBody: AuthBody): AuthResponse

}

@Serdeable
data class AuthBody(
    val token: String,
    val identity_provider: String? = "azuread",
)

@Serdeable
data class AuthResponse(
    val active: Boolean,
    val error: String? = "aok"
)
