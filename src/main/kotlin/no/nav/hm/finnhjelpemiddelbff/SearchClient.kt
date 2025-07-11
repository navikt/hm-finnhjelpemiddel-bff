package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("http://hm-grunndata-search/products/_search")
interface SearchClient {

    @Post(value = "/", produces = [io.micronaut.http.MediaType.APPLICATION_JSON])
    suspend fun getSeries(@Body searchBody: String): String


}
