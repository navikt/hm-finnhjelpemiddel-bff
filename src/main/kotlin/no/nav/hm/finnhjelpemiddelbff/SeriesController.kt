package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/series")
@Tag(name = "Series")
class SeriesController(
    private val searchService: SearchService
) {

    @Get("/{seriesId}")
    suspend fun getSeries(seriesId: String): HttpResponse<SearchResponse> {
        return HttpResponse.ok(
            searchService.getSeries(seriesId)
        )
    }

}


@Serdeable
data class SeriesResponse(val series: String)
