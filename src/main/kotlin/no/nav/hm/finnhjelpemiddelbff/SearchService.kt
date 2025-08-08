package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.time.LocalDateTime


@Singleton
class SearchService(
    private val searchClient: SearchClient
) {

    suspend fun getSeries(seriesId: String): Series? {
        val response = searchClient.getSeries(
            """
            {
                "query": {
                    "bool": {
                        "must": {
                            "term": {
                                "seriesId": "$seriesId"
                            }
                        }
                    }
                },
                "size": 150
            }
            """.trimIndent()
        )

        return parseSearchResponse(response)
    }

    private fun parseSearchResponse(searchResponse: SearchResponse): Series? {
        if (searchResponse.hits.hits.isEmpty()) return null

        val variants = searchResponse.hits.hits
        val firstVariant = searchResponse.hits.hits.first()._source

        val series = Series(
            id = firstVariant.seriesId,
            title = firstVariant.title,
            attributes = Attributes(
                manufacturer = firstVariant.attributes.manufacturer,
                articlename = firstVariant.attributes.articlename,
                series = firstVariant.attributes.series,
                shortdescription = firstVariant.attributes.shortdescription,
                text = firstVariant.attributes.text,
                compatibleWith = firstVariant.attributes.compatibleWith?.let {
                    CompatibleProducts(
                        seriesIds = it.seriesIds,
                        productIds = it.productIds
                    )
                },
                url = firstVariant.attributes.url,
            ),
            variantCount = variants.size,
            isoCategory = firstVariant.isoCategory,
            isoCategoryTitle = firstVariant.isoCategoryTitle,
            isoCategoryTitleInternational = firstVariant.isoCategoryTitleInternational,
            isoCategoryText = firstVariant.isoCategoryText,
            accessory = firstVariant.accessory,
            sparePart = firstVariant.sparePart,
            photos = firstVariant.media.filter { it.type === MediaType.IMAGE }.sortedBy { it.priority }.map {
                Image(
                    uri = it.uri
                )
            },
            videos = firstVariant.media.filter { it.type === MediaType.VIDEO }.sortedBy { it.priority }
                .map { Video(uri = it.uri, text = it.text) },
            documents = firstVariant.media.filter { it.type === MediaType.PDF }.sortedBy { it.priority }
                .map { Document(it.uri, it.text ?: "" , updated = it.updated) },
            supplierId = firstVariant.supplier.id,
            supplierName = firstVariant.supplier.name,
            agreements = parseAgreements(variants.flatMap { it._source.agreements }),
            main = firstVariant.main,
        )

        return series
    }

    private fun parseAgreements(agreementResponses: List<AgreementResponse>): List<AgreementInfo> {
        return agreementResponses.map { AgreementInfo(
                id = it.id.toString(),
                    title = it.title ?: "", //TODO
                    rank = it.rank,
                    postNr = it.postNr,
                    refNr = it.refNr,
                    postTitle = it.postTitle,
                    expired = it.expired,
        )}.distinct()
    }

}

@Serdeable
data class Series(
    val id: String,
    val title: String,
    val attributes: Attributes,
    val variantCount: Int,
    //val variants: ProductVariant[],
    val isoCategory: String,
    val isoCategoryTitle: String,
    val isoCategoryTitleInternational: String,
    val isoCategoryText: String,
    val accessory: Boolean,
    val sparePart: Boolean,
    val photos: List<Image>,
    val videos: List<Video>,
    val documents: List<Document>,
    val supplierId: String,
    val supplierName: String,
    val agreements: List<AgreementInfo>,
    val main: Boolean,
)

@Serdeable
data class Attributes(
    val manufacturer: String?,
    val articlename: String?,
    val series: String?,
    val shortdescription: String?,
    val text: String?,
    val compatibleWith: CompatibleProducts?,
    val url: String?,
)

@Serdeable
data class CompatibleProducts(
    val seriesIds: List<String> = emptyList(),
    val productIds: List<String> = emptyList()
)

@Serdeable
data class Image(
    val uri: String
)

@Serdeable
data class Video(
    val uri: String,
    val text: String?
)

@Serdeable
data class Document(
    val uri: String,
    val title: String,
    val updated: LocalDateTime?
)

@Serdeable
data class AgreementInfo(
    val id: String,
    val title: String,
    val rank: Int,
    val postNr: Int,
    val refNr: String?,
    val postTitle: String?,
    val expired: LocalDateTime
)