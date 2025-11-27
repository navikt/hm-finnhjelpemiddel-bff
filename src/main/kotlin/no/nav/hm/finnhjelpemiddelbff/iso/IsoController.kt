package no.nav.hm.finnhjelpemiddelbff.iso

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/iso")
@Tag(name = "Isos")
class IsoController(
    private val isoService: IsoService
) {

    @Get("/isoTree")
    suspend fun getIsoTree(): HttpResponse<IsoTree> = try {
        HttpResponse.ok(isoService.getIsoTree())
    } catch (exception: Exception) {
        HttpResponse.serverError()
    }
}


