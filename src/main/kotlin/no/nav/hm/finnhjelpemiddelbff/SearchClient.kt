package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("http://hm-grunndata-search/products/_search")
interface SearchClient {

    @Get(value = "/")
    suspend fun getSeries(@Body searchBody: String): String


}
