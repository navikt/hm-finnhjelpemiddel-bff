package no.nav.hm.finnhjelpemiddelbff.iso

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime

@Client("\${grunndata-db.url}")
interface GrunndataDbClient {
    @Get(value = "/api/v1/isocategories", produces = [io.micronaut.http.MediaType.APPLICATION_JSON])
    suspend fun getIsos(): List<IsoResponse>
}

@Serdeable
data class IsoResponse(
    val isoCode: String,
    val isoTitle: String,
    val isoText: String,
    val isoTextShort: String,
    val isoTranslations: IsoTranslations,
    val isoLevel: Int,
    val isActive: Boolean,
    val showTech: Boolean,
    val allowMulti: Boolean,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val searchWords: List<String>
)

@Serdeable
data class IsoTranslations(
    val titleEn: String,
    val textEn: String,
)