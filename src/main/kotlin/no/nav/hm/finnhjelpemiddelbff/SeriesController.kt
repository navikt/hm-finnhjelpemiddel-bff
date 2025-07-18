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
    suspend fun getSeries(seriesId: String): HttpResponse<SeriesResponse> {
        return HttpResponse.ok(
            SeriesResponse(searchService.getSeries(seriesId).hits.hits.first()._source.articleName)
        )
    }

}


@Serdeable
data class SeriesResponse(val series: String)
