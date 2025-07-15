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
@Client("\${search-url}")
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
    val created: String,
    val updated: String,
    val expired: String,
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
    val manufacturer: String?,
    val articlename: String?,
    val compatibleWith: CompatibleWithResponse?,
    val series: String?,
    val keywords: List<String>?,
    val shortdescription: String?,
    val text: String?,
    val url: String?,
    val tags: List<String>?,
    val bestillingsordning: Boolean?,
    val digitalSoknad: Boolean?,
)

@Serdeable
data class AgreementResponse(
    val id: UUID,
    val identifier: String?,
    val title: String?,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String?,
    val postTitle: String?,
    val postId: UUID?,
    val refNr: String?,
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
)

@Serdeable
data class CompatibleWithResponse(
    val seriesIds: List<String>,
    val productIds: List<String>,
)