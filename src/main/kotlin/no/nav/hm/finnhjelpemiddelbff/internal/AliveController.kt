package no.nav.hm.finnhjelpemiddelbff.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Hidden

@Controller("/internal")
@Hidden
class AliveController() {

    @Get("/isAlive")
    fun alive() = "ALIVE"

    @Get("/isReady")
    fun ready() = "OK"

}
