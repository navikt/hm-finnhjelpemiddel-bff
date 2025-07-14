package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.TechData

@Client("\${search-url}")
interface SearchClient {

    @Post(value = "/", produces = [io.micronaut.http.MediaType.APPLICATION_JSON])
    suspend fun getSeries(@Body searchBody: String): SearchResponse


}

@Serdeable
data class SearchResponse(
    val took: Int,
    val timed_out: Boolean,
    //shards
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
    //val data: List<TechData>,
    val main: Boolean,
    val media: List<MediaResponse>,
    val created: String,
    val updated: String,
    val expired: String,
    val createdBy: String,
    val updatedBy: String,
    //filters: { [key: string]: string },
    //val agreements: AgreementInfoResponse[],
    val hasAgreement: Boolean,


    )

@Serdeable
data class Supplier(
    val id: String,
    val identifier: String,
    val name: String,

)

//er alt optional?
@Serdeable
data class AttributeResponse (
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
data class MediaResponse(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
)

@Serdeable
data class CompatibleWithResponse (
    val seriesIds: List<String>,
    val productIds: List<String>,
)

/*
export interface SearchResponse {
  took: number
  timed_out: boolean
  _shards: object
  hits: {
    total: object
    hits: Hit[]
  }
}

export interface Hit {
  _index: string
  _type: string | null
  _id: string
  _score: string
  _source: ProductSourceResponse
}

export interface ProductSourceResponse {
  id: string
  articleName: string
  supplier: {
    id: string
    identifier: string
    name: string
  }
  title: string
  attributes: AttributeResponse
  status: 'INACTIVE' | 'ACTIVE'
  hmsArtNr: string | null
  identifier: string
  supplierRef: string
  isoCategory: string
  isoCategoryTitle: string
  isoCategoryTitleInternational: string
  isoCategoryText: string
  accessory: boolean
  sparePart: boolean
  seriesId: string
  data: Array<TechDataResponse>
  main: boolean
  media: MediaResponse[]
  created: string
  updated: string
  expired: string
  createdBy: string
  updatedBy: string
  filters: { [key: string]: string }
  agreements: AgreementInfoResponse[]
  hasAgreement: boolean
}
 */