package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("http://hm-grunndata-search/products/_search")
interface SearchClient {

    @Post(value = "/", produces = [io.micronaut.http.MediaType.APPLICATION_JSON])
    suspend fun getSeries(@Body searchBody: String): SearchResponse


}

@Introspected
data class SearchResponse(
    val took: Int,
    val timed_out: Boolean,
    //shards
    val hits: Hits
)

data class Hits(
    val hits: List<Hit>
)


data class Hit(
    val _source: ProductSourceResponse
)

data class ProductSourceResponse(
    val id: String,
    val articleName: String
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