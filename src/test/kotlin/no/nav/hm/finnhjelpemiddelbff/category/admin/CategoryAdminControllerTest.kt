package no.nav.hm.finnhjelpemiddelbff.category.admin

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.equality.shouldBeEqualUsingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelbff.auth.AuthResponse
import no.nav.hm.finnhjelpemiddelbff.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelbff.category.CategoryDto
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

        val id = UUID.randomUUID()

        val categoryDto = CategoryDto(id = id, data = objectMapper.readTree(data))

        runBlocking {
            categoryAdminController.createCategory(
                authorization = "auth",
                categoryDto = categoryDto
            ).status shouldBe HttpStatus.OK

            categoryAdminController.getCategoryById(id.toString())
                .shouldNotBeNull()
                .shouldBeEqualUsingFields(categoryDto)

            categoryAdminController.getCategories()
                .shouldNotBeEmpty()
                .shouldContainOnly(categoryDto)
        }
    }
}