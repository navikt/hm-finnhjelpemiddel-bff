package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.serde.annotation.Serdeable
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.TechData
import java.time.LocalDateTime
import java.util.UUID



@SerdeImport(
    value = TechData::class
)
@Client("\${search.url}")
interface SearchClient {

    @Post(value = "/", produces = [io.micronaut.http.MediaType.APPLICATION_JSON])
    suspend fun getSeries(@Body searchBody: String): SearchResponse


}

@Serdeable
data class SearchResponse(
    val took: Int,
    val timed_out: Boolean,
    val hits: Hits
)

@Serdeable
data class Hits(
    val hits: List<Hit>
)

@Serdeable
data class Hit(
    val _source: ProductSourceResponse
)

@Serdeable
data class ProductSourceResponse(
    val id: String,
    val articleName: String,
    val supplier: Supplier,
    val title: String,
    val attributes: AttributeResponse,
    val status: ProductStatus,
    val hmsArtNr: String?,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitle: String,
    val isoCategoryTitleInternational: String,
    val isoCategoryText: String,
    val accessory: Boolean,
    val sparePart: Boolean,
    val seriesId: String,
    val data: List<TechData>,
    val main: Boolean,
    val media: List<MediaResponse>,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    //filters: { [key: string]: string },
    val agreements: List<AgreementResponse>,
    val hasAgreement: Boolean,


    )

@Serdeable
data class Supplier(
    val id: String,
    val identifier: String,
    val name: String,

    )

@Serdeable
data class AttributeResponse(
    val manufacturer: String? = null,
    val articlename: String? = null,
    val compatibleWith: CompatibleWithResponse? = null,
    val series: String? = null,
    val keywords: List<String>? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    val tags: List<String>? = null,
    val bestillingsordning: Boolean? = null,
    val digitalSoknad: Boolean? = null,
)

@Serdeable
data class AgreementResponse(
    val id: UUID,
    val identifier: String? = null,
    val title: String? = null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String? = null,
    val postTitle: String? = null,
    val postId: UUID? = null,
    val refNr: String? = null,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
)

@Serdeable
data class MediaResponse(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
    val updated: LocalDateTime?
)

@Serdeable
data class CompatibleWithResponse(
    val seriesIds: List<String>,
    val productIds: List<String>,
)