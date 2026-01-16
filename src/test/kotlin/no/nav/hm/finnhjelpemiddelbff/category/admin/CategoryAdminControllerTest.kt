package no.nav.hm.finnhjelpemiddelbff.category.admin

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelbff.auth.AuthResponse
import no.nav.hm.finnhjelpemiddelbff.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelbff.category.CreateCategoryDto
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class CategoryAdminControllerTest(
    private val categoryAdminController: CategoryAdminController,
    private val objectMapper: ObjectMapper
) {

    @MockBean(AzureAdUserClient::class)
    fun mockAzureAdUserClient(): AzureAdUserClient = mockk<AzureAdUserClient>().apply {
        coEvery {
            validateToken(any())
        } answers {
            AuthResponse(active = true)
        }
    }

    @Test
    fun createCategory() {
        @Language("JSON") val data = """
            {
            "name": "Kategori",
            "description": "Dette er en kategori"
            }
        """.trimIndent()

        val categoryDto = CreateCategoryDto(data = objectMapper.readTree(data))

        runBlocking {
            val createCategoryResponse = categoryAdminController.createCategory(
                authorization = "auth",
                newCategoryDto = categoryDto
            )
            createCategoryResponse.status shouldBe HttpStatus.OK
            val categoryId = createCategoryResponse.body()

            categoryAdminController.getCategoryById(categoryId)
                .shouldNotBeNull()
                .data.shouldBe(categoryDto.data)

            categoryAdminController.getCategories().shouldNotBeEmpty()
                .shouldHaveSize(1)
                .first().data.shouldBe(categoryDto.data)
        }
    }

    @Test
    fun requireName() {
        @Language("JSON") val data = """
            {
            "description": "Dette er en kategori"
            }
        """.trimIndent()

        val categoryDto = CreateCategoryDto(data = objectMapper.readTree(data))

        runBlocking {
            categoryAdminController.createCategory(
                authorization = "auth",
                newCategoryDto = categoryDto
            ).status shouldBe HttpStatus.BAD_REQUEST
        }
    }
}