package no.nav.hm.finnhjelpemiddelbff

import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import java.time.LocalDateTime
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.ProductStatus

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

        return response.toProductSourceResponse().toSeries()
    }
}

fun SearchResponse.toProductSourceResponse(): List<ProductSourceResponse> = hits.hits.map { it._source }

fun List<ProductSourceResponse>.toSeries(): Series = Series(
    id = first().seriesId,
    title = first().title,
    attributes = Attributes(
        manufacturer = first().attributes.manufacturer,
        articlename = first().attributes.articlename,
        series = first().attributes.series,
        shortdescription = first().attributes.shortdescription,
        text = first().attributes.text,
        compatibleWith = first().attributes.compatibleWith?.let {
            CompatibleProducts(
                seriesIds = it.seriesIds,
                productIds = it.productIds
            )
        },
        url = first().attributes.url,
    ),
    variantCount = size,
    variants = map { variant ->
        Variant(
            id = variant.id,
            status = variant.status,
            hmsArtNr = variant.hmsArtNr,
            articleName = variant.articleName,
            techData = variant.data.associateBy(
                { it.key },
                { TechDataField(value = it.value, unit = it.unit) }),
            hasAgreement = variant.hasAgreement,
            expired = variant.expired,
            agreements = variant.agreements.toAgreementInfoList(),
            bestillingsordning = variant.attributes.bestillingsordning ?: false,
            digitalSoknad = variant.attributes.digitalSoknad ?: false,
            accessory = variant.accessory,
            sparePart = variant.sparePart,
        )
    },
    isoCategory = first().isoCategory,
    isoCategoryTitle = first().isoCategoryTitle,
    isoCategoryTitleInternational = first().isoCategoryTitleInternational,
    isoCategoryText = first().isoCategoryText,
    accessory = first().accessory,
    sparePart = first().sparePart,
    photos = first().media.filter { it.type === MediaType.IMAGE }.sortedBy { it.priority }.map {
        Image(
            uri = it.uri
        )
    },
    videos = first().media.filter { it.type === MediaType.VIDEO }.sortedBy { it.priority }
        .map { Video(uri = it.uri, text = it.text) },
    documents = first().media.filter { it.type === MediaType.PDF }.sortedBy { it.priority }
        .map { Document(it.uri, it.text ?: "", updated = it.updated) },
    supplierId = first().supplier.id,
    supplierName = first().supplier.name,
    agreements = flatMap { it.agreements }.toAgreementInfoList(),
    main = first().main,
)

fun List<AgreementResponse>.toAgreementInfoList(): List<AgreementInfo> = map {
    AgreementInfo(
        id = it.id.toString(),
        title = it.title ?: "", //TODO
        rank = it.rank,
        postNr = it.postNr,
        refNr = it.refNr,
        postTitle = it.postTitle,
        expired = it.expired,
    )
}.distinct()

@Serdeable
data class Series(
    val id: String,
    val title: String,
    val attributes: Attributes,
    val variantCount: Int,
    val variants: List<Variant>,
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
data class Variant(
    val id: String,
    val status: ProductStatus,
    val hmsArtNr: String?,
    val articleName: String,
    val techData: Map<String, TechDataField>,
    val hasAgreement: Boolean,
    val expired: LocalDateTime,
    val agreements: List<AgreementInfo>,
    val bestillingsordning: Boolean,
    val digitalSoknad: Boolean,
    val accessory: Boolean,
    val sparePart: Boolean,
)

@Serdeable
data class TechDataField(val value: String, val unit: String)

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