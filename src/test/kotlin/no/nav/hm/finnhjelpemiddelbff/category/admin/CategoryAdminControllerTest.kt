package no.nav.hm.finnhjelpemiddelbff.category.admin

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelbff.auth.AuthResponse
import no.nav.hm.finnhjelpemiddelbff.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelbff.category.CategoryData
import no.nav.hm.finnhjelpemiddelbff.category.CreateCategoryDto
import org.junit.jupiter.api.Test

@MicronautTest
class CategoryAdminControllerTest(
    private val categoryAdminController: CategoryAdminController,
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

        val categoryDto = CreateCategoryDto(title="Kategori", data = CategoryData(description = "Dette er en kategori"))

        runBlocking {
            val createCategoryResponse = categoryAdminController.createCategory(
                authorization = "auth",
                newCategoryDto = categoryDto
            )
            createCategoryResponse.status shouldBe HttpStatus.OK
            val categoryId = createCategoryResponse.body()

            categoryAdminController.getCategoryById(categoryId!!)
                .shouldNotBeNull()
                .data.shouldBe(categoryDto.data)

            categoryAdminController.getCategories().shouldNotBeEmpty()
                .shouldHaveSize(1)
                .first().data.shouldBe(categoryDto.data)
        }
    }


    @Test
    fun requireTitle() {
        runBlocking {
            categoryAdminController.createCategory(
                authorization = "auth",
                newCategoryDto = CreateCategoryDto(title="", data = CategoryData(description = "Dette er en kategori"))
            ).status shouldNotBe HttpStatus.OK
        }
    }
}