package no.nav.hm.finnhjelpemiddelbff

import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class SeriesControllerTest(private val seriesController: SeriesController) {


    @MockBean(SearchClient::class)
    fun mockSearchClient(): SearchClient = mockk<SearchClient>().apply {
        coEvery {
            getSeries(any())
        } answers {
            SearchResponse(
                5, false, Hits(
                    listOf(
                        Hit(
                            ProductSourceResponse(
                                id = "test",
                                articleName = "test",
                                supplier = Supplier("test", "test", "test"),
                                title = "test",
                                attributes = AttributeResponse(),
                                status = ProductStatus.ACTIVE,
                                hmsArtNr = "test",
                                identifier = "test",
                                supplierRef = "test",
                                isoCategory = "test",
                                isoCategoryTitle = "test",
                                isoCategoryTitleInternational = "test",
                                isoCategoryText = "test",
                                accessory = false,
                                sparePart = false,
                                seriesId = "seriesIdTest",
                                data = emptyList(),
                                main = true,
                                media = emptyList(),
                                created = LocalDateTime.now(),
                                updated = LocalDateTime.now(),
                                expired = LocalDateTime.now().plusDays(100),
                                createdBy = "test",
                                updatedBy = "test",
                                agreements = emptyList(),
                                hasAgreement = false

                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `series with single variant`() {
        runBlocking {
            val response = seriesController.getSeries("aaa")
            response.body().id shouldBe "seriesIdTest"
        }
    }
}