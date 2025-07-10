package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "hm-finnhjelpemiddel-bff",
        version = "1.0",
        description = "Bff for Finnhjelpemiddel"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.finnhjelpemiddelbff")
            .mainClass(Application.javaClass)
            .start()
    }
}

